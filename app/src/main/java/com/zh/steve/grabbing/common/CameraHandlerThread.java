package com.zh.steve.grabbing.common;

import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.zh.steve.grabbing.ui.CameraWindow;

// This code is mostly based on the top answer here: http://stackoverflow.com/questions/18149964/best-use-of-handlerthread-over-other-similar-classes
public class CameraHandlerThread extends HandlerThread {
    private static final String TAG = "CameraHandlerThread";

    private CameraWindow mCameraWindow;

    public CameraHandlerThread(CameraWindow cameraWindow) {
        super("CameraHandlerThread");
        mCameraWindow = cameraWindow;
        start();
    }

    public void startCamera(final int cameraId) {
        Log.d(TAG, "open camera in new thread");
        Handler localHandler = new Handler(getLooper());
        localHandler.post(new Runnable() {
            @Override
            public void run() {
                final Camera camera = CameraUtils.getCameraInstance(cameraId);
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCameraWindow.setupCameraPreview(camera);
                    }
                });
            }
        });
    }
}
