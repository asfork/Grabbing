package com.zh.steve.grabbing.ui;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * Created by Steve Zhang
 * 1/19/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */
public class CameraWindow {
    private static final String TAG = "CameraWindow";

    private static WindowManager windowManager;
    private static SurfaceView dummyCameraView;
    private static Context mContext;

    /**
     * 显示全局窗口
     *
     * @param context
     */
    public static void showCameraWindow(Context context) {
        if (CameraWindow.mContext == null) {
            CameraWindow.mContext = context.getApplicationContext();
            windowManager = (WindowManager) CameraWindow.mContext
                    .getSystemService(Context.WINDOW_SERVICE);
            dummyCameraView = new SurfaceView(CameraWindow.mContext);
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
        }
    }

    /**
     * @return 获取窗口视图
     */
    public static SurfaceView getDummyCameraView() {
        return dummyCameraView;
    }

    /**
     * 隐藏窗口
     */
    public static void dismissCameraWindow() {
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
