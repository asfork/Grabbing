package com.zh.steve.grabbing.services;

/**
 * Created by Steve Zhang
 * 1/13/14
 * <p/>
 * If it works, I created it. If not, I didn't.
 */

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.zh.steve.grabbing.Constants;
import com.zh.steve.grabbing.common.ServerAddressCallback;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
 * Linux command to send UDP:
 * #socat - UDP-DATAGRAM:192.168.1.255:11111,broadcast,sp=11111
 */
public class UDPListenerService extends Service {
    private static final String TAG = "UDPListenerService";
    private Boolean shouldRestartSocketListen = true;

    //Boolean shouldListenForUDPBroadcast = false;
    DatagramSocket socket;

    /**
     * 回调接口
     */
    private ServerAddressCallback serverAddressCallback;

    /**
     * 注册回调接口的方法，供外部调用
     *
     * @param serverAddressCallback
     */
    public void setServerAddressCallback(ServerAddressCallback serverAddressCallback) {
        this.serverAddressCallback = serverAddressCallback;
    }

    private void listenAndWaitAndThrowIntent(InetAddress broadcastIP, Integer port) throws Exception {
        byte[] recvBuf = new byte[1024];
        if (socket == null || socket.isClosed()) {
//            socket = new DatagramSocket(port, broadcastIP);
            socket = new DatagramSocket(port);
            socket.setBroadcast(true);
        }
        //socket.setSoTimeout(1000);
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
        Log.d(TAG, "Waiting for UDP broadcast");
        socket.receive(packet);

        String senderIP = packet.getAddress().getHostAddress();
        String message = new String(packet.getData()).trim();

        Log.d(TAG, "Got UDB broadcast from " + senderIP + ", message: " + message);

//        broadcastIntent(senderIP, message);
//        socket.close();

        // 服务器地址发生变化通知调用方
        if (serverAddressCallback != null) {
            serverAddressCallback.updateServerAddress(senderIP);
        }

        if (message.equals("grab")) {
            startCameraService();
        }
    }

    private void startCameraService() {
        Intent intent = new Intent(getApplicationContext(), CameraService.class);
        getApplicationContext().startService(intent);
    }

    void startListenForUDPBroadcast() {
        Thread UDPBroadcastThread = new Thread(new Runnable() {
            public void run() {
                try {
                    // send and receive by the udp IP group
                    InetAddress broadcastIP = InetAddress.getByName("192.168.3.255");
                    while (shouldRestartSocketListen) {
                        listenAndWaitAndThrowIntent(broadcastIP, Constants.LISTEN_PORT);
                    }
                    //if (!shouldListenForUDPBroadcast) throw new ThreadDeath();
                } catch (Exception e) {
                    Log.i(TAG, "no longer listening for UDP broadcasts cause of error " + e.getMessage());
                }
            }
        });
        UDPBroadcastThread.start();
    }

    void stopListen() {
        shouldRestartSocketListen = false;

        try {
            socket.close();
        } catch (Exception e) {
            Log.d(TAG, e.toString(), e);
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Stop for udp broadcast");
        stopListen();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shouldRestartSocketListen = true;
        startListenForUDPBroadcast();
        Log.e("UDP", "Service started");
        return START_STICKY;
    }

    /**
     * 返回一个Binder对象
     */
    @Override
    public IBinder onBind(Intent intent) {
        shouldRestartSocketListen = true;
        startListenForUDPBroadcast();
        Log.e("UDP", "Service started");
        return new UDPListenerBinder();
    }

    public class UDPListenerBinder extends Binder {
        /**
         * 获取当前Service的实例
         *
         * @return
         */
        public UDPListenerService getService() {
            return UDPListenerService.this;
        }
    }
}