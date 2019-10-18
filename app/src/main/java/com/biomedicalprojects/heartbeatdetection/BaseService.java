package com.biomedicalprojects.heartbeatdetection;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;

import java.util.Date;

public class BaseService  extends Service {

    protected String logText(String text){
        return StaticHelper.TERMINAL_MESSAGE += "[Log - "+ new Date().getHours()+":"+ new Date().getMinutes()+":"+ new Date().getSeconds()+" ] "+text+" \n";
    }
    protected String logText(String desc ,String text){
        return StaticHelper.TERMINAL_MESSAGE += "[Log - "+ new Date().getHours()+":"+ new Date().getMinutes()+":"+ new Date().getSeconds()+" ] "+desc+" "+text+" \n";
    }

    protected String phoneNumber() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getString("phoneNumber", "09126192191");
    }

    protected String bradycardiaNumber() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getString("bradycardia", "59");
    }

    protected String tachycardiaNumber() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getString("tachycardia", "120");
    }

    protected boolean isSendSMS() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean("smsSetting", true);
    }

    protected boolean isLocation() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean("location", true);
    }

    protected String bluetoothName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getString("bluetooth", "HC-05");
    }

    protected void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
