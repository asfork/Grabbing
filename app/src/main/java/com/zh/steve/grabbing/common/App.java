package com.zh.steve.grabbing.common;

import android.app.Application;

/**
 * Created by Steve Zhang
 * 1/19/16
 * <p/>
 * If it works, I created it. If not, I didn't.
 */
public class App extends Application {
    private String serverAddress;

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }
}
