package com.biomedicalprojects.heartbeatdetection;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.biomedicalprojects.heartbeatdetection.enums.ConnectionStatus;
import com.biomedicalprojects.heartbeatdetection.exception.BluetoothIsOffException;
import com.biomedicalprojects.heartbeatdetection.exception.DataFormatException;
import com.biomedicalprojects.heartbeatdetection.exception.NoBluetoothAdapterException;
import com.biomedicalprojects.heartbeatdetection.exception.NoPairedDeviceException;
import com.biomedicalprojects.heartbeatdetection.exception.PairedDeviceNotFoundException;
import com.biomedicalprojects.heartbeatdetection.exception.SocketCloseException;
import com.biomedicalprojects.heartbeatdetection.exception.SocketConnectionException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static com.biomedicalprojects.heartbeatdetection.StaticHelper.stopWorker;

public class MainService extends BaseService {
    private BluetoothAdapter mBluetoothAdapter;

    private byte[] readBuffer;
    private int readBufferPosition;

    private LocationManager locationManager;
    private LocationListener listener;
    private String link;
    private boolean connectionFlag = false;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private Thread workerThread;
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        logText("Service Destroy");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        logText("Service Created");
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        try {
            if (!connectionFlag) {
                StaticHelper.connectionStatus = ConnectionStatus.LOADING;
                findBT();
                openBT();
                beginListenForData();
                StaticHelper.connectionStatus = ConnectionStatus.CONNECT;
                connectionFlag = true;
            } else {
                closeBT();
                StaticHelper.connectionStatus = ConnectionStatus.DISCONNECT;
                connectionFlag = false;
            }

        }catch (BluetoothIsOffException e) {
            e.printStackTrace();
            logText("Bluetooth is off");
            StaticHelper.connectionStatus = ConnectionStatus.DISCONNECT;

        } catch (SocketConnectionException e) {
            e.printStackTrace();
            logText("Can't connect to Socket");
            StaticHelper.connectionStatus = ConnectionStatus.DISCONNECT;
        } catch (DataFormatException e) {
            e.printStackTrace();
            logText("Data format is wrong");
            StaticHelper.connectionStatus = ConnectionStatus.DISCONNECT;

        } catch (NoBluetoothAdapterException e) {
            e.printStackTrace();
            logText("No bluetooth adapter available");
            StaticHelper.connectionStatus = ConnectionStatus.DISCONNECT;
        } catch (NoPairedDeviceException e) {
            e.printStackTrace();
            logText("Paired Device is Null");
            StaticHelper.connectionStatus = ConnectionStatus.DISCONNECT;
        } catch (PairedDeviceNotFoundException e) {
            e.printStackTrace();
            logText("Paired Device Not Found");
            StaticHelper.connectionStatus = ConnectionStatus.DISCONNECT;
        } catch (SocketCloseException e) {
            e.printStackTrace();
            logText("error in closing socket");
            StaticHelper.connectionStatus = ConnectionStatus.DISCONNECT;
        } catch (NullPointerException e) {
            e.printStackTrace();
            logText("Something is wrong",e.getMessage());
            StaticHelper.connectionStatus = ConnectionStatus.DISCONNECT;
        } catch (Exception e) {
            e.printStackTrace();
            logText("Something is wrong",e.getMessage());
            StaticHelper.connectionStatus = ConnectionStatus.DISCONNECT;

        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    protected void findBT() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.i("HomeFragment findBT", "No bluetooth adapter available");
            throw new NoBluetoothAdapterException();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            throw new BluetoothIsOffException();
        }
        boolean flag = false;
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(bluetoothName())) {
                    mmDevice = device;

                    flag = true;
                    break;
                }
            }
        } else {
            throw new NoPairedDeviceException();
        }
        if (flag) {
            Log.i("HomeFragment findBT", "Bluetooth Device Found");
            Log.i("MAC Addres", mmDevice.getAddress());
            logText("Paired Device Found");
            logText("Bluetooth Name", mmDevice.getName());
            logText("MAC Address", mmDevice.getAddress());
        } else {
            Log.i("HomeFragment findBT", "Bluetooth Device Not Found");
            throw new PairedDeviceNotFoundException();
        }


    }


    protected void openBT() {
        try {
            UUID uuid = mmDevice.getUuids()[0].getUuid();
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mBluetoothAdapter.cancelDiscovery();
            logText("Connecting to Socket using " + uuid);
            mmSocket.connect();
            logText("Socket is connected");
            if (!mmSocket.isConnected()) {
                throw new SocketConnectionException();
            }

            mmOutputStream = new DataOutputStream(mmSocket.getOutputStream());
            mmInputStream = new DataInputStream(mmSocket.getInputStream());



        }catch (IOException e){
            throw new SocketConnectionException();
        }


    }

    protected void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    final String res = data.substring(0,data.length()-1);
                                    Log.i("Data",res);
                                    logText("Data", res);

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            Log.i("rrrrrrrr",res);
                                            heartBeatDetection(Integer.parseInt(res));
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void closeBT() {
        try {
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            StaticHelper.TERMINAL_MESSAGE = "";
            logText("Socket is Closed");
        } catch (IOException e) {
            e.printStackTrace();
            throw new SocketCloseException();
        }
    }


    private void heartBeatDetection(int heartBeat) {
        try {
            String sms = null;
            String location = link;
            if (heartBeat > Integer.parseInt(tachycardiaNumber())) {
                if (isSendSMS()) {
                    if (isLocation()) {
                        sms = getString(R.string.tachycardia_sms);
                        sms += "\n" + location;
                    } else {
                        sms = getString(R.string.tachycardia_sms);
                    }
                    sendSMS(phoneNumber(), sms);
                    logText("Tachycardia", tachycardiaNumber());
                    logText("Send sms to", phoneNumber());
                }
            } else if (heartBeat < Integer.parseInt(bradycardiaNumber())) {
                if (isSendSMS()) {
                    if (isLocation()) {
                        sms = getString(R.string.bradycardia_sms);
                        sms += "\n" + location;
                    } else {
                        sms = getString(R.string.bradycardia_sms);
                    }
                    sendSMS(phoneNumber(), sms);
                    logText("Bradycardia", bradycardiaNumber());
                    logText("Send sms to", phoneNumber());
                }
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("erroorrr",e.getMessage());
        }
    }


}
