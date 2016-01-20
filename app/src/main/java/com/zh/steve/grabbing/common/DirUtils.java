package com.zh.steve.grabbing.common;

import android.os.Environment;

import java.io.File;

/**
 * Created by Steve Zhang
 * 1/19/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */
public class DirUtils {
    public static File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "Grabbing");
    }
}
