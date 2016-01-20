package com.zh.steve.grabbing;

/**
 * Created by Steve Zhang
 * 1/13/13
 * <p>
 * If it works, I created it. If not, I didn't.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.zh.steve.grabbing.ui.CameraWindow;

public class CameraService extends Service {
    private static final String TAG = "CameraService";
    private static final String FLASH_STATE = "FLASH_STATE";
    private static final String AUTO_FOCUS_STATE = "AUTO_FOCUS_STATE";
    private static final String CAMERA_ID = "CAMERA_ID";

    private CameraWindow mCameraWindow;

    private BroadcastReceiver picTakenResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.RESULT_IMG_TAKEN)) {
                String fileName = intent.getStringExtra(Constants.EXTRA_IMG_NAME);
                UploadImgService.startUploadImg(context, fileName);
                stopSelf();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int mCameraId = -1;

        Log.d(TAG, "onStartCommand...");

        if (intent != null) {
            mCameraId = intent.getIntExtra(CAMERA_ID, -1);
        } else {
            mCameraId = -1;
        }

        mCameraWindow = new CameraWindow(this);
        mCameraWindow.startCamera(mCameraId);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy...");

        mCameraWindow.stopCamera();
        mCameraWindow.dismissCameraWindow();
        unregisterReceiver(picTakenResultReceiver);
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.RESULT_IMG_TAKEN);
        registerReceiver(picTakenResultReceiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
