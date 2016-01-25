package com.zh.steve.grabbing.common;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by Steve Zhang
 * 1/25/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */
public class UploadedHandlerThread implements Runnable {
    private static final String TAG = "UploadedHandlerThread";
    Socket socket;
    String photoPath;

    public UploadedHandlerThread(Socket socket, String photoPath) {
        this.socket = socket;
        this.photoPath = photoPath;
    }

    public void run() {
        if (socket.isBound()) {
            Log.d(TAG, "Connect successful");
            try {
                try {
                    socket.setKeepAlive(true);
                    socket.setSoTimeout(3 * 1000);
                    BufferedInputStream bufin = new BufferedInputStream(new FileInputStream(photoPath));
                    BufferedOutputStream bufout = new BufferedOutputStream(socket.getOutputStream());
                    BufferedInputStream confirm_txt = new BufferedInputStream(socket.getInputStream());
                    byte[] bufdata = new byte[1024];
                    int len;
                    while ((len = bufin.read(bufdata)) != -1) {
                        bufout.write(bufdata, 0, len);
                        bufout.flush();
                    }
                    socket.shutdownOutput();
                    Log.d(TAG, "Uploaded has done");
                    len = confirm_txt.read(bufdata, 0, bufdata.length);
                    String str = new String(bufdata, 0, len);
                    Log.d(TAG, str);
                    socket.close();
                    bufin.close();
                    bufout.close();
                    confirm_txt.close();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
