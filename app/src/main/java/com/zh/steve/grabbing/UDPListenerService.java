package com.zh.steve.grabbing;

/**
 * Created by Steve Zhang
 * 1/13/14
 * <p/>
 * If it works, I created it. If not, I didn't.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.zh.steve.grabbing.common.ServerAddressCallback;
import com.zh.steve.grabbing.ui.MainActivity;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
 * Linux command to send UDP:
 * #socat - UDP-DATAGRAM:192.168.1.255:11111,broadcast,sp=11111
 */
public class UDPListenerService extends Service {
    private static final String TAG = "UDPListenerService";
    private PowerManager.WakeLock mWakeLock;
    private WifiManager.WifiLock mWifiLock;
    private WifiManager.MulticastLock mMulticastLock;
    private Boolean shouldRestartSocketListen = true;
    //Boolean shouldListenForUDPBroadcast = false;
    DatagramSocket socket;

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "PartialWakeLockTag");
        mWakeLock.acquire();
        Log.d(TAG, "Acquiring wake lock");

        WifiManager wm = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "FullWifiModeTag");
        mWifiLock.acquire();
        Log.d(TAG, "Acquiring Wifi lock");

        mMulticastLock = wm.createMulticastLock("MulticastLockTag");
        mMulticastLock.acquire();
        Log.d(TAG, "Acquiring Multicast lock");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shouldRestartSocketListen = true;
        startListenForUDPBroadcast();
        Log.e("UDP", "Service started");

        startForcegroundService();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopListen();
        Log.d(TAG, "Stop for udp broadcast");

        if (mWakeLock != null) {
            mWakeLock.release();
            Log.d(TAG, "Release wake lock");
        }
        if (mWifiLock != null) {
            mWifiLock.release();
            Log.d(TAG, "Release Wifi lock");
        }
        if (mMulticastLock != null) {
            mMulticastLock.release();
            Log.d(TAG, "Release multicast lock");
        }
        stopForeground(true);
    }

    private void startForcegroundService() {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_info))
                .setContentIntent(pendingIntent).build();

        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, notification);
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