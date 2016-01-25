package com.zh.steve.grabbing;

/**
 * Created by Steve Zhang
 * 1/19/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */
public class Constants {
    public static final int THREAD_SLEEP_TIME = 1000;
    public static final int LOCAL_PORT = 9056;
    public static final int HOST_PORT = 8888;
    public static final String UPLOAD_PATH = "/AndroidFileUpload/fileUpload.php";

    public static final String ACTION_GRAB = "grab";
    public static final String ACTION_REGISTER = "devices";
    public static final String STATUS_DEVICE = "available";

    public static final String EXTRA_IMG_NAME = "com.zh.steve.grabbing.extra.IMG_NAME";
    public static final String RESULT_IMG_TAKEN = "com.zh.steve.grabbing.result.IMG_TAKEN";
    public static final String ACTION_UPLOAD_IMG = "com.zh.steve.grabbing.action.UPLOAD_IMAGE";
    public static final String ACTION_DESTORY_SERVICE = "com.zh.steve.grabbing.action.DESTORY_UDP";

    public interface NOTIFICATION_ID {
        int FOREGROUND_SERVICE = 101;
    }
}
