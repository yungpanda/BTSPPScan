package com.art4l.btsppscan;

import android.Manifest;
import android.app.Activity;
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
import android.util.Log;

public class HoneywellBTScanner {

    private static final String TAG = HoneywellBTScanner.class.getSimpleName();
    private static final boolean D = false;

    //Bluetooth service
    private ServiceManager btService;
    private int bTRetryCounter = 0;					//retry when there are problems with Bluetooth Channel
    private static final int MAXRETRY = 2000;			//retry 2000 times
    private String mMacAddress;
    private Activity mContext;
    private boolean isBTStarted = false;
    private BluetoothAdapter mBluetoothAdapter;
    public final static int REQUEST_COARSE_LOCATION = 1;



    // return event
    private OnMessageReceived mMessageListener;
    private OnConnectionStatus mConnectionListener;



    public HoneywellBTScanner(Activity context){
        mContext = context;

    }

    public void initiateScanner(final String macAddress){

        mMacAddress = macAddress;

        btService = new ServiceManager(mContext,BTServer.class, new Handler(){
            @Override
            public void handleMessage(Message msg){

                switch (msg.what) {

                    case BTServer.STATE_NONE:
                    case BTServer.STATE_LISTEN:
                        //give instruction to press on button
                        if (D) Log.d(TAG,"Maken van verbinding met Ringscanner..");
                        break;
                    case BTServer.STATE_CONNECTED:
                        // show that scanner is connected

                        bTRetryCounter = 0;		//reset retry counter
                        isBTStarted = true;

                        if (mConnectionListener != null) {
                            mConnectionListener.onConnected();
                        }

                        break;
                    case BTServer.STATE_BARCODE:				//scanned data returned
                        ScanResult scanResult = new ScanResult();
                        scanResult.setBarcodeType("generic barcode");
                        scanResult.setBarcodeMessage(((String)msg.obj).trim());
                        mMessageListener.messageReceived("BarcodeScan",scanResult);

                        break;
                    case BTServer.STATE_LOST:
                        if (D) Log.d(TAG,"Connection lost");

                        try {
                            btService.send(Message.obtain(null, BTServer.REQ_RESTART_BTSERVER, null));		//try to open connection
                            // show connection with scanner is lost

                        }catch(RemoteException ex){

                        }
                        break;
                    case BTServer.STATE_NOBT:			//problem with BT COnnection
                        if (D) Log.d(TAG,"No BT Connection, retry");
                        bTRetryCounter++;
                        if (bTRetryCounter == MAXRETRY) {
                            if (D) Log.d(TAG,"Connection Lost with scanner");
                            mMessageListener.errorReceived("NOCONNECT","Connection lost with scanner");


                            isBTStarted = false;
                            proceedDiscovery();
                            //go back into discovery mode
                            if (mConnectionListener != null) {
                                mConnectionListener.onDisconnected(false);
                            }

                            break;

                        }
                        try {
                            if (mConnectionListener) {
                                mConnectionListener.onDisconnected(true);
                            }

                            btService.send(Message.obtain(null, BTServer.REQ_RESTART_BTSERVER, null));		//try to open connection
                            // show connection with scanner is lost

                        }catch(RemoteException ex){

                        }
                        break;
                    case BTServer.REQ_MACADDRESS:
                        if (D) Log.d(TAG,"Mac Address requested: "+ macAddress);
                        try {
                            btService.send(Message.obtain(null, BTServer.MACADDRESS, macAddress));
                            btService.send(Message.obtain(null, BTServer.REQ_RESTART_BTSERVER, null));

                        } catch (RemoteException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        break;

                    default:
                        super.handleMessage(msg);
                }


            }



        });

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter);

    }

    public void startScanner(){
        btService.start();

    }

    public void stopScanner(){
        btService.stop();

        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException ex){

        }
    }


    public void setMessageListener(OnMessageReceived messageListener){
        mMessageListener = messageListener;

    }

    public void setConnectionListener(OnConnectionStatus connectionListener){
        mConnectionListener = connectionListener;
    }



    public void proceedDiscovery() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        mBluetoothAdapter.startDiscovery();

    }

    /**
     * Send a command to the scanner
     *
     * @param command
     */

    public void sendCommand(String command){

        byte[] bCommand = command.getBytes();
        byte[] writeOut = new byte[1008];


        byte[] name = ":XENON:".getBytes();

        writeOut[0] =22;
        writeOut[1] =77;
        writeOut[2] =13;
//      for (int i=0; i<name.length;i++){
//          writeOut[3+i] = name[i];
//      }

        for (int i=0; i<bCommand.length;i++){
            writeOut[3+i] = bCommand[i];
        }

        try {
            btService.send(Message.obtain(null, BTServer.WRITE_SETTING,new String(writeOut)));
        }catch(RemoteException ex) {

        }
    }

    // The BroadcastReceiver that listens for discovered devices and
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (D) Log.d(TAG, "BT Received: " + device.getName() + " " + device.getAddress()+ " "+ isBTStarted);
                //check if this has the same address as the one expected and start service if is was not started yet.
                if (device.getAddress().equalsIgnoreCase(mMacAddress) && !isBTStarted) {
                    mBluetoothAdapter.cancelDiscovery();
                    HoneywellBTScanner.this.startScanner();
                }
            }

        }
    };

    /**
     * Check if the permission is set to look for the BT Devices
     *
     * @return
     */
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(mContext, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
            return false;
        }
        return true;
    }



    /**
     *
     * Declare the interface to receive data
     *
     *
     */

    public interface OnMessageReceived {
        void messageReceived(String type, ScanResult message);
        void errorReceived(String type, String errorMessage);
    }

    public interface OnConnectionStatus{
        void onConnected();
        void onDisconnected(boolean isRetrying);

    }
}
