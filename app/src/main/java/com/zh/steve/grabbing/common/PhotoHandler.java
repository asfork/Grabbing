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
import android.widget.Toast;

import com.zh.steve.grabbing.Constants;
import com.zh.steve.grabbing.utils.PathUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoHandler implements PictureCallback {
    private final Context context;

    public PhotoHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        PathUtil pathUtil = new PathUtil();
        File pictureFileDir = pathUtil.getDir();
        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT);
        String date = dateFormat.format(new Date());
        String photoFile = "Picture_" + date + ".jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        File pictureFile = new File(filename);
        System.out.println("filename is " + filename);

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            Toast.makeText(context, "New Image saved:" + photoFile,
                    Toast.LENGTH_LONG).show();

            // 成功后发送广播
            Intent intent = new Intent(Constants.RESULT_IMG_TAKEN);
            intent.putExtra(Constants.EXTRA_IMG_NAME, photoFile);
            context.sendBroadcast(intent);
        } catch (Exception error) {
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }
    }
}