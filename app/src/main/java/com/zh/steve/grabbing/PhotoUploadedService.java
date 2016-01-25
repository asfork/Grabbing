package com.zh.steve.grabbing;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.zh.steve.grabbing.common.App;
import com.zh.steve.grabbing.common.UploadedHandlerThread;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.Socket;

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
public class PhotoUploadedService extends IntentService {
    private static final String TAG = "PhotoUploadedService";
    private String serverAddress;
    private String fileName;


    public static void startUploadImg(Context context, String fileName) {
        Intent intent = new Intent(context, PhotoUploadedService.class);
        intent.setAction(Constants.ACTION_UPLOAD_IMG);
        intent.putExtra(Constants.EXTRA_IMG_NAME, fileName);
        context.startService(intent);
    }

    public PhotoUploadedService() {
        super("PhotoUploadedService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent...");
        if (intent != null) {
            if (intent.getAction().equals(Constants.ACTION_UPLOAD_IMG)) {
                fileName = intent.getStringExtra(Constants.EXTRA_IMG_NAME);
                App app = (App) getApplicationContext();
                serverAddress = app.getServerAddress();
                handleUploadPhoto(serverAddress, fileName);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy...");
    }

    private void handleUploadPhoto(String hostAddress, String photoName) {
        try {
            Socket socket = new Socket(hostAddress, Constants.HOST_PORT);
            String photoPath = getPath(photoName);
            if (socket.isBound()) {
                new Thread(new UploadedHandlerThread(socket, photoPath)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUploadImg(String serverAddress, String fileName) {
        OkHttpClient okHttpClient = new OkHttpClient();
        try {
            File file = new File(getPath(fileName));
            String contentType = file.toURL().openConnection().getContentType();
            RequestBody fileBody = RequestBody.create(MediaType.parse(contentType), file);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", fileName, fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url(getUploadAddress(serverAddress))
                    .post(requestBody)
                    .build();

            Log.d(TAG, "Uploaded address is:" + getUploadAddress(serverAddress));
            Response response = okHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code" + response);

            Log.d(TAG, response.body().string());
//            parseJSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseJSONObject(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            boolean error = jsonObject.getBoolean("error");
            String msg = jsonObject.getString("message");
            Log.d(TAG, msg);

            //TODO send uploaded result to host
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPath(String fileName) {
        return getDir().getPath() + File.separator + fileName;
    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "Grabbing");
    }

    public String getUploadAddress(String serverAddress) {
        return "http://" + serverAddress + ":" + Constants.HOST_PORT + Constants.UPLOAD_PATH;
    }
}
