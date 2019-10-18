package com.biomedicalprojects.heartbeatdetection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.view.View;

import com.biomedicalprojects.heartbeatdetection.enums.ConnectionStatus;

import java.io.InputStream;
import java.io.OutputStream;

public class StaticHelper {
    public static String TERMINAL_MESSAGE= "";
    public static boolean CONNECTION_STATUS= false;
    public static BluetoothSocket mmSocket;
    public static BluetoothDevice mmDevice;
    public static OutputStream mmOutputStream;
    public static InputStream mmInputStream;
    static volatile boolean stopWorker;
    public static ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECT;
    public static int tachycardiaNumber;
    public static int bradycardiaNumber;
    public static boolean isSms ;
    public static boolean isLocation;
    public static View v;
    public static Context context ;
    public static String phone;
    public static int[] lineDatas = new int[11];
    public static String sms = "09128922899";
    public static String link = "link";;
}
