package com.fmtech.fmlive;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fmtech.fmlive.push.LivePusher;

public class MainActivity extends AppCompatActivity {
    
    private static String URL = "rtmp://send1a.douyu.com/live/" +
            "999565rDN17wQEOF?wsSecret=f76118ec9b7fa3e5514004b5e4f4bc85&wsTime=5b5ac68d&wsSeek=off&wm=0&tw=0";

    private LivePusher mLivePusher;
    private SurfaceView mSurfaceView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.live_surfaceview);
        
        checkPermission();

    }

    public void checkPermission(){
        if (Build.VERSION.SDK_INT>22){
            if (checkSelfPermission(android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED
                    ||checkSelfPermission(Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, 100);
            }else {
                init();
            }
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


    public void start(View view) {
        Button btn = (Button)view;
        if(btn.getText().equals("开始直播")){
            mLivePusher.startPush();
            btn.setText("停止直播");
        }else{
            mLivePusher.stopPush();
            btn.setText("开始直播");
        }
    }

    public void switchVideo(View view) {
        mLivePusher.switchCamera();
    }
    

}
