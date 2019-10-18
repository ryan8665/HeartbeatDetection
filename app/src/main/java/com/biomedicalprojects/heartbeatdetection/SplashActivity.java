package com.biomedicalprojects.heartbeatdetection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

public class SplashActivity extends Activity {
    private View view;
    private TextView about;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        about = findViewById(R.id.about);
        String aboutText = about.getText() +"\n Version: "+ appVersion();
        about.setText(aboutText);

        Thread background = new Thread() {
            public void run() {

                try {
                    sleep(1*1000);

                } catch (Exception e) {

                }finally {
                    StaticHelper.TERMINAL_MESSAGE += "[Log - "+ new Date().getHours()+":"+ new Date().getMinutes()+":"+ new Date().getSeconds()+" ] Start. \n";
                    Intent i=new Intent(getApplication(),MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };

        background.start();
    }



    protected String appVersion() {
        try {
            PackageInfo pInfo = getApplication().getPackageManager().getPackageInfo(this.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }

    }
}
