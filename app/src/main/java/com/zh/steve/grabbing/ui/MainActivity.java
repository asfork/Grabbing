package com.zh.steve.grabbing.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zh.steve.grabbing.R;
import com.zh.steve.grabbing.common.App;
import com.zh.steve.grabbing.common.ServerAddressCallback;
import com.zh.steve.grabbing.services.UDPListenerService;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TextView textView;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个 service 对象
            UDPListenerService udpListenerService = ((UDPListenerService.UDPListenerBinder) service).getService();

            //注册回调接口来接收服务器地址的变化
            udpListenerService.setServerAddressCallback(new ServerAddressCallback() {
                @Override
                public void updateServerAddress(String ip) {
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("ip", ip);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.tv_ip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Button startUDP = (Button) findViewById(R.id.start_udp);
        startUDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent udpIntent = new Intent(MainActivity.this, UDPListenerService.class);
                bindService(udpIntent, conn, Context.BIND_AUTO_CREATE);
            }
        });

        Button stopUDP = (Button) findViewById(R.id.stop_udp);
        stopUDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unbindService(conn);
                textView.setText(null);
            }
        });

        Button startCamera = (Button) findViewById(R.id.start_camera);
        startCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent("com.zh.steve.grabbing.ACTION_CAMERA");
                startService(cameraIntent);
            }
        });

        Button uploadImg = (Button) findViewById(R.id.upload_img);
        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (conn != null) {
            try {
                unbindService(conn);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String ip = bundle.getString("ip");
            textView.setText("Current server address:" + ip);
            App app = (App) getApplicationContext();
            app.setServerAddress(ip);
        }
    };
}
