package com.zh.steve.grabbing.services;

/**
 * Created by Steve Zhang
 * 1/13/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceView;

import com.zh.steve.grabbing.Constants;
import com.zh.steve.grabbing.common.PhotoHandler;
import com.zh.steve.grabbing.ui.CameraWindow;
import com.zh.steve.grabbing.utils.CamParaUtil;

import java.util.List;

public class CameraService extends Service {
    private static final String TAG = "CameraService";
    private Camera mCamera;
    private SurfaceView preview;
    private boolean isRunning; // 是否已在监控拍照
    private boolean isAutoFocus = false;

    private BroadcastReceiver picTakenResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.PIC_TAKEN_RESULT)) {
                stopSelf();
            }
        }
    };

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate() {
        super.onCreate();

        CameraWindow.showCameraWindow(getApplicationContext());
        registerReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand...");
        startTakePic();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy...");
        isRunning = false;
        releaseCamera();

        unregisterReceiver(picTakenResultReceiver);
        CameraWindow.dismissCameraWindow();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.PIC_TAKEN_RESULT);
        registerReceiver(picTakenResultReceiver, filter);
    }

    private void startTakePic() {
        if (!isRunning) {
            preview = CameraWindow.getDummyCameraView();
            if (preview != null) {
                autoTakePic();
            } else {
                stopSelf();
            }
        }
    }

    private void autoTakePic() {
        Log.d(TAG, "autoTakePic...");
        isRunning = true;

        //这里得开线程打开摄像头
        new Thread(new Runnable() {
            @Override
            public void run() {
                //初始化camera并对焦拍照
                initCamera();
            }
        }).start();
    }

    //初始化摄像头
    private void initCamera() {
        if (isAutoFocus) {
            mCamera.stopPreview();
        }
        //如果存在摄像头
        if (checkCameraHardware(getApplicationContext())) {
            //获取摄像头（首选后置）
            if (openFacingBackCamera()) {
                Log.d(TAG, "openCameraSuccess");

                Camera.Parameters mParam = mCamera.getParameters();
                mParam.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
                //设置大小和方向等参数
                List<Camera.Size> supportedPictureSizes = mParam.getSupportedPictureSizes();
                Camera.Size pictureSize = CamParaUtil.getInstance().getPictureSize(supportedPictureSizes, 2000);
                mParam.setPictureSize(pictureSize.width, pictureSize.height);
                // 保持摄像头持续自动对焦
                mParam.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera.setParameters(mParam);
//                mCamera.setDisplayOrientation(90);

                try {
                    mCamera.setPreviewDisplay(preview.getHolder());
                    mCamera.startPreview();// 开始预览
                } catch (Exception e) {
                    e.printStackTrace();
                    releaseCamera();
                    stopSelf();
                }

                setAutoFocus();
            } else {
                Log.d(TAG, "openCameraFailed");
                stopSelf();
            }
        }
    }

    private void setAutoFocus() {
        Log.d(TAG, "AutoFocusing");

        try {
            //因为开启摄像头需要时间，这里让线程睡两秒
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        mCamera.autoFocus(mAutoFocusCallback);
        isAutoFocus = true;

        takePicture();
    }

    // Mimic continuous auto-focusing
    private Camera.AutoFocusCallback mAutoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            Log.d(TAG, "Autofocus success");
        }
    };

    private void takePicture() {
        Log.d(TAG, "takePicture...");
        try {
            mCamera.takePicture(null, null, new PhotoHandler(getApplicationContext()));
        } catch (Exception e) {
            Log.d(TAG, "takePicture failed!");
            e.printStackTrace();
            throw e;
        }
    }

    //判断是否存在摄像头
    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    //得到后置摄像头
    private boolean openFacingBackCamera() {
        //尝试开启后置摄像头
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    Log.d(TAG, "tryToOpenCamera");
                    mCamera = Camera.open(camIdx);
                    return true;
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            Log.d(TAG, "releaseCamera...");
            mCamera.cancelAutoFocus();
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;

            isAutoFocus = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
