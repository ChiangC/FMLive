package com.fmtech.fmlive;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fmtech.fmlive.push.LivePusher;

public class MainActivity extends AppCompatActivity {
    
    private static String URL = "rtmp://send1a.douyu.com/live/" +
            "999565rLHPgotne7?wsSecret=db275d3ff30e8036cccecd93faeb836e&wsTime=5b6185d7&wsSeek=off&wm=0&tw=0";

    private LivePusher mLivePusher;
    private SurfaceView mSurfaceView;
    private Button mPushBtn;
    private Button mSwitchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.live_surfaceview);
        mPushBtn = findViewById(R.id.btn_push);
        mSwitchBtn = findViewById(R.id.btn_switch);

        initViews();

        checkPermission();

    }

    private void initViews(){
        mPushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start(v);
            }
        });

        mSwitchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 100);
        }else {
            init();
        }
    }


    private void init(){
        mLivePusher = new LivePusher(MainActivity.this, mSurfaceView.getHolder(), URL);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            init();
        }
    }


    private void start(View view) {
        Button btn = (Button)view;
        if(btn.getText().equals("开始直播")){
            mLivePusher.startPush();
            btn.setText("停止直播");
        }else{
            mLivePusher.stopPush();
            btn.setText("开始直播");
        }
    }

    private void switchCamera() {
        mLivePusher.switchCamera();
    }
    

}
