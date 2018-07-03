package com.art4l.btsppscan;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    HoneywellBTScanner honeywellBTScanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ProgressBar progressBar = findViewById(R.id.progressBar);

        honeywellBTScanner = new HoneywellBTScanner(this);
        //IP Adress of Honeywell Scanner
        //Use the setting: Pair with a Honeywell Mobile Computer in the manuals
        String macAddress = "00:10:20:3E:CC:94";


        honeywellBTScanner.initiateScanner(macAddress);



    }

    @Override
    protected void onResume() {
        super.onResume();

        //check if the permissions are set to find the BT devices, if not it is handled by onRequestPermissionsResult
        if (honeywellBTScanner.checkLocationPermission()) honeywellBTScanner.startScanner();


        //catch the messages
        honeywellBTScanner.setMessageListener(new HoneywellBTScanner.OnMessageReceived() {
            @Override
            public void messageReceived(String type, ScanResult message) {
                Log.d(TAG, "Barcode Received: " + message.getBarcodeMessage());
            }

            @Override
            public void errorReceived(String type, String errorMessage) {
                Log.d(TAG,"Error Recieved: " + errorMessage);

            }
        });

        //catch connectstatus
        honeywellBTScanner.setConnectionListener(new HoneywellBTScanner.OnConnectionStatus() {
            @Override
            public void onConnected() {
                ProgressBar progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.INVISIBLE);

                //connected, send a command
                honeywellBTScanner.sendCommand("PAPHHF!");  //command for manual trigger mode
//                honeywellBTScanner.sendCommand("PAPSPE!");    //command for Streaming presentation mode, enhance; PAPSPN! for standard


            }

            @Override
            public void onDisconnected(boolean isRetrying) {
                ProgressBar progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);


            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

        honeywellBTScanner.stopScanner();

    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {

            case HoneywellBTScanner.REQUEST_COARSE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    honeywellBTScanner.startScanner();
                    break;
                }
            }

        }
    }
}