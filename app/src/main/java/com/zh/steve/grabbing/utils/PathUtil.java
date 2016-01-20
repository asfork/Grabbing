package com.zh.steve.grabbing.utils;

import android.os.Environment;

import com.zh.steve.grabbing.Constants;

import java.io.File;

/**
 * Created by Steve Zhang
 * 1/19/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */
public class PathUtil {
    public File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "Grabbing");
    }

    public String getPath(String fileName) {
        return getDir().getPath() + File.separator + fileName;
    }

    public String getUploadAddress(String serverAddress) {
        return "http://" + serverAddress + ":" + Constants.UPLOAD_SERVER_PORT + Constants.UPLOAD_PATH;
    }
}
