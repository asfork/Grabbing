package com.zh.steve.grabbing.ui;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.zh.steve.grabbing.common.CameraHandlerThread;
import com.zh.steve.grabbing.common.CameraUtils;

/**
 * Created by Steve Zhang
 * 1/15/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */
public class CameraWindow {
    private static final String TAG = "CameraWindow";

    private WindowManager windowManager;
    private CameraPreview dummyCameraView;
    private CameraHandlerThread mCameraHandlerThread;
    private Context mContext;
    private Camera mCamera;

    private Boolean mFlashState = true;
    private boolean mAutofocusState = true;

    public CameraWindow(Context context) {
        mContext = context;
    }

    public void startCamera(int cameraId) {
        if (this.mCameraHandlerThread == null) {
            this.mCameraHandlerThread = new CameraHandlerThread(this);
        }

        this.mCameraHandlerThread.startCamera(cameraId);
    }

    public void setupCameraPreview(Camera camera) {
        mCamera = camera;
        if (mCamera != null) {
            Log.d(TAG, "Open camera successfully");
            if (mFlashState != null) {
                setFlash(mFlashState);
            }

            showCameraWindow();
        }
    }

    public void setFlash(boolean flag) {
        mFlashState = flag;
        if (mCamera != null && CameraUtils.isFlashSupported(mCamera)) {

            Camera.Parameters parameters = mCamera.getParameters();
            if (flag) {
                if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_AUTO)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            } else {
                if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_AUTO)) {
                    return;
                }
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            mCamera.setParameters(parameters);
        }
    }

    /**
     * Show preview window
     *
     */
    private void showCameraWindow() {
        if (mCamera != null) {
            windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            dummyCameraView = new CameraPreview(mContext, mCamera);
            LayoutParams params = new LayoutParams();
            params.width = 1;
            params.height = 1;
            params.alpha = 0;
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
            // 屏蔽点击事件
            params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE
                    | LayoutParams.FLAG_NOT_TOUCHABLE;
            windowManager.addView(dummyCameraView, params);
            Log.d(TAG, TAG + " showing");

            setAutoFocus(mAutofocusState);
        }
    }

    public void setAutoFocus(boolean state) {
        mAutofocusState = state;
        if (dummyCameraView != null) {
            dummyCameraView.setAutoFocus(state);
        }
    }

    public void stopCamera() {
        if (mCamera != null) {
            dummyCameraView.stopCameraPreview();
            dummyCameraView.setCamera(null);
            mCamera.release();
            mCamera = null;
        }
        if (mCameraHandlerThread != null) {
            mCameraHandlerThread.quit();
            mCameraHandlerThread = null;
        }
    }

    public void stopCameraPreview() {
        if (dummyCameraView != null) {
            dummyCameraView.stopCameraPreview();
        }
    }

    /**
     * @return 获取窗口视图
     */
    public CameraPreview getDummyCameraView() {
        return dummyCameraView;
    }

    /**
     * 隐藏窗口
     */
    public void dismissCameraWindow() {
        try {
            if (windowManager != null && dummyCameraView != null) {
                windowManager.removeView(dummyCameraView);
                Log.d(TAG, TAG + " dismissed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
