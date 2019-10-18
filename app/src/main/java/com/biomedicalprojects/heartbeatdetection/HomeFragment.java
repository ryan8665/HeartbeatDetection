package com.biomedicalprojects.heartbeatdetection;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PieChartView;

import static android.content.Context.LOCATION_SERVICE;
import static com.biomedicalprojects.heartbeatdetection.StaticHelper.bradycardiaNumber;
import static com.biomedicalprojects.heartbeatdetection.StaticHelper.lineDatas;
import static com.biomedicalprojects.heartbeatdetection.StaticHelper.link;
import static com.biomedicalprojects.heartbeatdetection.StaticHelper.sms;
import static com.biomedicalprojects.heartbeatdetection.StaticHelper.v;


public class HomeFragment extends BaseFragment {

    private TextView mainText;
    private TextView normal;
    private TextView bradycardia;
    private TextView tachycardia;
    private Button sendSms;
    private  boolean smsFlag ;
    //
    private LineChartView linechart;
    private LineChartData linedata;
    private int maxNumberOfLines = 1;
    private int numberOfPoints = 10;

    float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];
    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    //

     private CardView cardView;

    private ImageView mainImage;
    private TextView beat;
    private TextView details;
    private BluetoothAdapter mBluetoothAdapter;

    private byte[] readBuffer;
    private int readBufferPosition;
    volatile boolean stopWorker;
    private LocationManager locationManager;
    private LocationListener listener;
    boolean connectionFlag = false;
    ProgressBar progressBar;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_home, container, false);
        beat = (TextView) v.findViewById(R.id.beat);
        details = (TextView) v.findViewById(R.id.details);
        mainImage = v.findViewById(R.id.imageView);
        bradycardia = v.findViewById(R.id.textBradycardia);

        cardView = v.findViewById(R.id.card_receive);
        linechart = v.findViewById(R.id.line_chart);
        bradycardia.setText("Bradycardia: Slow Heart Rate, Rate < " + bradycardiaNumber());
        normal = v.findViewById(R.id.textNormal);
        normal.setText("Normal: Normal Heart Rate, " + bradycardiaNumber() + " > Rate > " + tachycardiaNumber());
        tachycardia = v.findViewById(R.id.textTachycardia);
        tachycardia.setText("Tachycardia: Fast Heart Rate, Rate > " + tachycardiaNumber());
        progressBar = v.findViewById(R.id.progressBar);
        beat.setText("0 bpm");
        details.setText("");
        smsFlag = true;
        sendSms = v.findViewById(R.id.sendSms);
        sendSms.setVisibility(View.GONE);
        sendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               sendSMS(StaticHelper.phone, StaticHelper.sms);
            }
        });
        StaticHelper.tachycardiaNumber = Integer.parseInt(tachycardiaNumber());
        StaticHelper.bradycardiaNumber = Integer.parseInt(bradycardiaNumber());
        StaticHelper.isLocation = isLocation();
        StaticHelper.isSms = isSendSMS();
        StaticHelper.phone = phoneNumber();
        StaticHelper.context = getContext();
        bradycardiaNumber();
//        initLineData();
//        generatelineData(0);
        connectionFlag = StaticHelper.CONNECTION_STATUS;
        if (StaticHelper.CONNECTION_STATUS) {
            mainImage.setImageResource(R.drawable.ic_connect);
            generatelineData(0,false);
            cardView.setVisibility(View.VISIBLE);
        } else {
            mainImage.setImageResource(R.drawable.ic_disconnect);
        initLineData();
        generatelineData(0,false);
            cardView.setVisibility(View.GONE);
        }
        if (!hasPermissions(getContext(), PERMISSIONS)) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_ALL);
        }

        if (isLocation()) {
            locationManager = (LocationManager) v.getContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    10,
                    500, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            link = "http://maps.google.com/maps?q=loc:" + location.getLatitude() + "," + location.getLongitude();
                            logText("Location", link);
                            Log.i("link", link);
                        }

                        @Override
                        public void onStatusChanged(String s, int i, Bundle bundle) {
                            //
                        }

                        @Override
                        public void onProviderEnabled(String s) {
                            //
                        }

                        @Override
                        public void onProviderDisabled(String s) {
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    });
        }




        mainImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (isServiceRunning(MainService.class)) {
//                    getActivity().startService(new Intent(getContext(), MainService.class));
//                } else {
//                    getActivity().stopService(new Intent(getContext(), MainService.class));
//                }

                new prepare().execute("");

            }
        });

        return v;
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


        } catch (IOException e) {
            throw new SocketConnectionException();
        }


    }

    protected void findBT() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.i("HomeFragment findBT", "No bluetooth adapter available");
            throw new NoBluetoothAdapterException();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
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


    protected void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 10;
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    final String res = data.substring(0, data.length() - 1);
                                    Log.i("Data", res);
                                    logText("Data", res);

                                    handler.post(new Runnable() {
                                        public void run() {
                                            heartBeatDetection(Integer.parseInt(res));
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
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
            beat.setText("0 bpm");
            details.setText("");

        } catch (IOException e) {
            e.printStackTrace();
            throw new SocketCloseException();
        }
    }

    private void heartBeatDetection(int heartBeat) {
        try {
            generatelineData(heartBeat,true);
            beat = (TextView) v.findViewById(R.id.beat);
            details = (TextView) v.findViewById(R.id.details);

            linechart = v.findViewById(R.id.line_chart);

            String location = link;
            beat.setText(Integer.toString(heartBeat) + " bpm");
            if (heartBeat > StaticHelper.tachycardiaNumber) {
                if (StaticHelper.isSms) {
                    if (StaticHelper.isLocation) {
                        sendSms.setVisibility(View.GONE);
                        sms =  sms = heartBeat+"bpm - Tachycardia";
                        sms += "\n" + location;
                    } else {
                        sms =  sms = heartBeat+"bpm - Tachycardia";
                    }
                    if (smsFlag){
                        sendSMS(StaticHelper.phone, sms);
                        smsFlag = false;
                    }
                    logText("Tachycardia", StaticHelper.tachycardiaNumber + "");
                    logText("Send sms to", StaticHelper.phone);
                }
                details.setText(StaticHelper.context.getString(R.string.tachycardia));
            } else if (heartBeat < StaticHelper.bradycardiaNumber) {
                if (StaticHelper.isSms) {
                    if (StaticHelper.isLocation) {
                        sendSms.setVisibility(View.GONE);
                        sms = heartBeat+"bpm - Bradycardia";
                        sms += "\n" + location;
                    } else {
                        sms = heartBeat+"bpm - Bradycardia";
                    }
                    if (smsFlag){
                        sendSMS(StaticHelper.phone, sms);
                        smsFlag = false;
                    }
                    logText("Bradycardia", StaticHelper.bradycardiaNumber + "");
                    logText("Send sms to", StaticHelper.phone);
                }
                details.setText(StaticHelper.context.getString(R.string.bradycardia));
            } else {
                sendSms.setVisibility(View.GONE);
                details.setText(StaticHelper.context.getString(R.string.normal));

            }
        } catch (Exception e) {
            e.printStackTrace();
            logText("Error:", e.getMessage());
            Log.e("Error", e.getMessage());
        }
    }


    private class prepare extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            if (Looper.myLooper() == null) {
                Looper.prepare();
            }
            try {
                if (!connectionFlag) {
                    findBT();
                    openBT();
                    connectionFlag = true;
                } else {
                    closeBT();
                    connectionFlag = false;
                }

            } catch (BluetoothIsOffException e) {
                e.printStackTrace();
                logText("Bluetooth is off");

            } catch (SocketConnectionException e) {
                e.printStackTrace();
                logText("Can't connect to Socket");

            } catch (DataFormatException e) {
                e.printStackTrace();
                logText("Data format is wrong");

            } catch (NoBluetoothAdapterException e) {
                e.printStackTrace();
                logText("No bluetooth adapter available");

            } catch (NoPairedDeviceException e) {
                e.printStackTrace();
                logText("Paired Device is Null");

            } catch (PairedDeviceNotFoundException e) {
                e.printStackTrace();
                logText("Paired Device Not Found");

            } catch (SocketCloseException e) {
                e.printStackTrace();
                logText("error in closing socket");

            } catch (NullPointerException e) {
                e.printStackTrace();
                logText("Something is wrong", e.getMessage());

            } catch (Exception e) {
                e.printStackTrace();
                logText("Something is wrong", e.getMessage());

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            mainImage.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            progressBar.setVisibility(View.GONE);
            mainImage.setVisibility(View.VISIBLE);
            if (connectionFlag) {
                mainImage.setImageResource(R.drawable.ic_connect);
                StaticHelper.CONNECTION_STATUS = true;
                stopWorker = false;
                beginListenForData();
                cardView.setVisibility(View.VISIBLE);
            } else {
                mainImage.setImageResource(R.drawable.ic_disconnect);
                StaticHelper.CONNECTION_STATUS = false;
                stopWorker = true;
                lineDatas = new int[11];
                initLineData();
                generatelineData(0,false);
                cardView.setVisibility(View.GONE);
            }
            super.onPostExecute(s);
        }
    }

    protected boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    logText("Request for permission", permission);
                    return false;
                }
            }
        }
        return true;
    }

    private void generatelineData(int heartBeat,boolean flag) {
        if(flag){
            int[] temp = new int[numberOfPoints];
            for (int i = 0; i < numberOfPoints; i++) {
                if (i == 0) {
                    temp[0] = heartBeat;
                }else {

                    temp[i] = lineDatas[i-1];
                }
            }
            lineDatas = temp;
        }



        List<Line> lines = new ArrayList<Line>();

        List<PointValue> values = new ArrayList<PointValue>();

       for (int i=0 ;i < numberOfPoints ;i++){
           values.add(new PointValue(i, lineDatas[i]));
       }
        Line line = new Line(values);

        line.setShape(ValueShape.CIRCLE);
        line.setCubic(true);
        line.setFilled(true);
        line.setHasLabels(true);
        line.setHasLabelsOnlyForSelected(false);
        line.setHasLines(true);
        line.setHasPoints(true);
        lines.add(line);


        linedata = new LineChartData(lines);

        if (hasAxes) {
            Axis axisY = new Axis().setHasLines(false);
            Axis axisX = new Axis().setHasLines(false);

            axisY.setMaxLabelChars(1);
//            linedata.setAxisXBottom(axisX);
//            linedata.setAxisYLeft(axisY);
 //           axisY.setName(getResources().getString(R.string.axisY));

        } else {
            // linedata.setAxisXBottom(null);
            linedata.setAxisYLeft(null);
        }

        linedata.setBaseValue(Integer.MAX_VALUE);
        linechart.setLineChartData(linedata);

    }

    private void initLineData() {
        for (int i = 0; i < numberOfPoints; i++) {
            lineDatas[i] = 0;
        }
    }

    private void prepareLineData(int data) {

    }


}



