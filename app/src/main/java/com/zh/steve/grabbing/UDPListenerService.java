package com.zh.steve.grabbing;

/**
 * Created by Steve Zhang
 * 1/13/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


/*
 * Linux command to send UDP:
 * #socat - UDP-DATAGRAM:192.168.1.255:11111,broadcast,sp=11111
 */
public class UDPListenerService extends Service {
    static String UDP_BROADCAST = "UDPBroadcast";

    //Boolean shouldListenForUDPBroadcast = false;
    DatagramSocket socket;

    private void listenAndWaitAndThrowIntent(InetAddress broadcastIP, Integer port) throws Exception {
        byte[] recvBuf = new byte[1024];
        if (socket == null || socket.isClosed()) {
            socket = new DatagramSocket(port, broadcastIP);
            socket.setBroadcast(true);
        }
        //socket.setSoTimeout(1000);
        DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
        Log.e("UDP", "Waiting for UDP broadcast");
        socket.receive(packet);

        String senderIP = packet.getAddress().getHostAddress();
        String message = new String(packet.getData()).trim();

        Log.e("UDP", "Got UDB broadcast from " + senderIP + ", message: " + message);

        broadcastIntent(senderIP, message);
        socket.close();
    }

    private void broadcastIntent(String senderIP, String message) {
        Intent intent = new Intent(UDPListenerService.UDP_BROADCAST);
        intent.putExtra("sender", senderIP);
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }

    void startListenForUDPBroadcast() {
        Thread UDPBroadcastThread = new Thread(new Runnable() {
            public void run() {
                try {
                    InetAddress broadcastIP = InetAddress.getByName("192.168.139.255"); //172.16.238.42 //192.168.1.255
                    Integer port = 9056;
                    while (shouldRestartSocketListen) {
                        listenAndWaitAndThrowIntent(broadcastIP, port);
                    }
                    //if (!shouldListenForUDPBroadcast) throw new ThreadDeath();
                } catch (Exception e) {
                    Log.i("UDP", "no longer listening for UDP broadcasts cause of error " + e.getMessage());
                }
            }
        });
        UDPBroadcastThread.start();
    }

    private Boolean shouldRestartSocketListen=true;

    void stopListen() {
        shouldRestartSocketListen = false;
        socket.close();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDestroy() {
        stopListen();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        shouldRestartSocketListen = true;
        startListenForUDPBroadcast();
        Log.i("UDP", "Service started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}