package com.zh.steve.grabbing.common;

/**
 * Created by Steve Zhang
 * 1/13/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.zh.steve.grabbing.Constants;

import java.io.File;
import java.io.FileOutputStream;

public class PhotoHandler implements PictureCallback {
    private static final String TAG = "PhotoHandler";
    private final Context mContext;

    public PhotoHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        File pictureFileDir = getDir();
        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
            Log.d(TAG, "Can't create directory to save image.");
            return;
        }

        String imei = ((TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        String picName = imei + ".jpg";
        String picPath = pictureFileDir.getPath() + File.separator + picName;

        File pictureFile = new File(picPath);
        Log.d(TAG, "filename is " + picPath);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile, false);
            fos.write(data);
            fos.close();
            Log.d(TAG, "New Image saved:" + picName);

            // 成功后发送广播
            Intent intent = new Intent(Constants.RESULT_IMG_TAKEN);
            intent.putExtra(Constants.EXTRA_IMG_NAME, picName);
            mContext.sendBroadcast(intent);
        } catch (Exception error) {
            Log.d(TAG, "Image could not be saved.");
        }
    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "Grabbing");
    }
}