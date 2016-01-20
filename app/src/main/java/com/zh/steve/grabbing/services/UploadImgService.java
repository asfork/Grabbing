package com.zh.steve.grabbing.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zh.steve.grabbing.Constants;
import com.zh.steve.grabbing.common.App;
import com.zh.steve.grabbing.utils.PathUtil;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Steve Zhang
 * 1/17/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */
public class UploadImgService extends IntentService {
    private static final String TAG = "UploadImgService";
    private String serverAddress;
    private String fileName;
    private int failure_times = 0;


    public static void startUploadImg(Context context, String fileName) {
        Intent intent = new Intent(context, UploadImgService.class);
        intent.setAction(Constants.ACTION_UPLOAD_IMG);
        intent.putExtra(Constants.EXTRA_IMG_NAME, fileName);
        context.startService(intent);
    }

    public UploadImgService() {
        super("UploadImgService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent...");
        if (intent != null) {
            if (intent.getAction().equals(Constants.ACTION_UPLOAD_IMG)) {
                fileName = intent.getStringExtra(Constants.EXTRA_IMG_NAME);
                App app = (App) getApplicationContext();
                serverAddress = app.getServerAddress();
                handleUploadImg(serverAddress, fileName);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy...");
    }

    private void handleUploadImg(String serverAddress, String fileName) {
        OkHttpClient okHttpClient = new OkHttpClient();
        try {
            PathUtil pathUtil = new PathUtil();
            File file = new File(pathUtil.getPath(fileName));
            String contentType = file.toURL().openConnection().getContentType();
            RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", fileName, fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(pathUtil.getUploadAddress(serverAddress))
                    .post(requestBody)
                    .build();

            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code" + response);

            Log.d(TAG, response.body().string());
            parseJSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseJSONObject(String jsonData) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(jsonData).getAsJsonObject();
            boolean error = jsonObject.get("error").getAsBoolean();
            String msg = jsonObject.get("message").getAsString();
            Log.d(TAG, msg);

            // 上传失败后重新上传
            if (!error && failure_times <= 3) {
                handleUploadImg(serverAddress, fileName);
                failure_times++;
            } else if (failure_times > 3) {
//                    sendBroadcastToServer();
                //TODO
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
