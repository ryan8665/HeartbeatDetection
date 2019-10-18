package com.biomedicalprojects.heartbeatdetection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.View;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class BaseFragment extends Fragment {
    static BluetoothSocket mmSocket;
    static BluetoothDevice mmDevice;
    static OutputStream mmOutputStream;
    static InputStream mmInputStream;
    static Thread workerThread;

    protected boolean isNetworkAvailable() {
        if (getActivity() != null) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }



    protected String appVersion() {
        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }

    }

    protected String phoneNumber() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        return prefs.getString("phoneNumber", "09126192191");
    }

    protected String bradycardiaNumber() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        return prefs.getString("bradycardia", "59");
    }

    protected String tachycardiaNumber() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        return prefs.getString("tachycardia", "120");
    }

    protected boolean isSendSMS() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        return prefs.getBoolean("smsSetting", true);
    }

    protected boolean isLocation() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        return prefs.getBoolean("location", true);
    }

    protected String bluetoothName() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        return prefs.getString("bluetooth", "HC-05");
    }

    protected void sendSMS(String phoneNumber, String message) {
        try {

            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, message, null, null);
            logText("Send sms to", StaticHelper.phone);
        }catch (Exception e){
            StaticHelper.TERMINAL_MESSAGE += e.toString();
        }

    }

    protected String logText(String text){
        return StaticHelper.TERMINAL_MESSAGE += "[Log - "+ new Date().getHours()+":"+ new Date().getMinutes()+":"+ new Date().getSeconds()+" ] "+text+" \n";
    }
    protected String logText(String desc ,String text){
        return StaticHelper.TERMINAL_MESSAGE += "[Log - "+ new Date().getHours()+":"+ new Date().getMinutes()+":"+ new Date().getSeconds()+" ] "+desc+" "+text+" \n";
    }

    protected boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
