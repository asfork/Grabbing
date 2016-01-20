package com.zh.steve.grabbing.ui;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zh.steve.grabbing.Constants;
import com.zh.steve.grabbing.common.CamParaUtils;
import com.zh.steve.grabbing.common.PhotoHandler;

import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private Camera mCamera;
    private Handler mAutoFocusHandler;
    private Context mContext;
    private boolean mPreviewing = true;
    private boolean mAutoFocus = true;
    private boolean mSurfaceCreated = false;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mContext = context;
        init(camera);
    }

    public CameraPreview(Context context, AttributeSet attrs, Camera camera) {
        super(context, attrs);
        init(camera);
    }

    public void init(Camera camera) {
        setCamera(camera);
        mAutoFocusHandler = new Handler();
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        stopCameraPreview();
        showCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mSurfaceCreated = false;
        stopCameraPreview();
    }

    public void showCameraPreview() {
        if (mCamera != null) {
            try {
                getHolder().addCallback(this);
                mPreviewing = true;
                setupCameraParameters();
                mCamera.setPreviewDisplay(getHolder());
                mCamera.startPreview();
                if (mAutoFocus) {
                    if (mSurfaceCreated) { // check if surface created before using autofocus
                        safeAutoFocus();
                    } else {
                        scheduleAutoFocus(); // wait 1 sec and then do check again
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    private void takePicture() {
        if (mPreviewing && (mCamera != null)) {
            // 线程阻塞一会，保证摄像头启动成功
            try {
                Thread.sleep(Constants.THREAD_SLEEP_TIME);
            } catch (Exception e) {
                Log.d(TAG, e.toString(), e);
            }

            Log.d(TAG, "takePicture...");
            mCamera.takePicture(null, null, new PhotoHandler(mContext));
        }
    }

    public void safeAutoFocus() {
        try {
            mCamera.autoFocus(autoFocusCB);

            takePicture();
        } catch (RuntimeException re) {
            // Horrible hack to deal with autofocus errors on Sony devices
            // See https://github.com/dm77/barcodescanner/issues/7 for example
            scheduleAutoFocus(); // wait 1 sec and then do check again
        }
    }

    public void stopCameraPreview() {
        if (mCamera != null) {
            try {
                mPreviewing = false;
                getHolder().removeCallback(this);
                mCamera.cancelAutoFocus();
                mCamera.setOneShotPreviewCallback(null);
                mCamera.stopPreview();
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    public void setupCameraParameters() {
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        Camera.Size pictureSize = CamParaUtils.getInstance().getMaxPictureSize(supportedPictureSizes);
        parameters.setPictureFormat(PixelFormat.JPEG);//设置拍照后存储的图片格式
        parameters.setPictureSize(pictureSize.width, pictureSize.height);
        // 保持摄像头持续自动对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(parameters);
    }

    public void setAutoFocus(boolean state) {
        if (mCamera != null && mPreviewing) {
            if (state == mAutoFocus) {
                return;
            }
            mAutoFocus = state;
            if (mAutoFocus) {
                if (mSurfaceCreated) { // check if surface created before using autofocus
                    Log.v(TAG, "Starting autofocus");
                    safeAutoFocus();
                } else {
                    scheduleAutoFocus(); // wait 1 sec and then do check again
                }
            } else {
                Log.v(TAG, "Cancelling autofocus");
                mCamera.cancelAutoFocus();
            }
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (mCamera != null && mPreviewing && mAutoFocus && mSurfaceCreated) {
                safeAutoFocus();
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            scheduleAutoFocus();
        }
    };

    private void scheduleAutoFocus() {
        mAutoFocusHandler.postDelayed(doAutoFocus, 1000);
    }
}