package com.art4l.btsppscan;

import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    //Bluetooth service
//    ServiceManager btService;
//    private int bTRetryCounter = 0;					//retry when there are problems with Bluetooth Channel
//    private static final int MAXRETRY = 10;			//retry 10 times

    HoneywellBTScanner honeywellBTScanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        honeywellBTScanner = new HoneywellBTScanner(this);
        //IP Adress of Honeywell Scanner
        //Use the setting: Pair with a Honeywell Mobile Computer in the manuals
        String macAddress ="00:10:20:3E:CC:94";

        honeywellBTScanner.initiateScanner(macAddress);

/*
        btService = new ServiceManager(this,BTServer.class, new Handler(){
            @Override
            public void handleMessage(Message msg){

                switch (msg.what) {

                    case BTServer.STATE_NONE:
                    case BTServer.STATE_LISTEN:
                        //give instruction to press on button
                        Log.d(TAG,"Maken van verbinding met Ringscanner..");
                        break;
                    case BTServer.STATE_CONNECTED:
                        // show that scanner is connected

                        bTRetryCounter = 0;		//reset retry counter
                        break;
                    case BTServer.STATE_BARCODE:				//scanned data returned
                        Log.d(TAG,"Barcode received: " + (String)msg.obj);

                        //test
                        try {
                            btService.send(Message.obtain(null, BTServer.WRITE_SETTING,"BEPRPT4!"));
                            // show connection with scanner is lost

                        }catch(RemoteException ex) {

                        }

                        break;
                    case BTServer.STATE_LOST:
                        Log.d(TAG,"Connection lost");

                        try {
                            btService.send(Message.obtain(null, BTServer.REQ_RESTART_BTSERVER, null));		//try to open connection
                            // show connection with scanner is lost

                        }catch(RemoteException ex){

                        }
                        break;
                    case BTServer.STATE_NOBT:			//problem with BT COnnection
                        Log.d(TAG,"No BT Connection");
                        bTRetryCounter++;
                        try {
                            Thread.sleep(2000);			//wait 2 seconds
                        } catch (InterruptedException e){

                        }
                        if (bTRetryCounter == MAXRETRY) {

                            Toast.makeText(MainActivity.this,"Geen verbinding met Ringscanner!!!",Toast.LENGTH_LONG).show();
                            break;
                        }
                        try {
                            btService.send(Message.obtain(null, BTServer.REQ_RESTART_BTSERVER, null));		//try to open connection
                            // show connection with scanner is lost

                        }catch(RemoteException ex){

                        }
                        break;
                    case BTServer.REQ_MACADDRESS:
                        Log.d(TAG,"Mac Address requested: "+ macAddress);
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

        btService.start();
*/
        honeywellBTScanner.startScanner();

        //catch the messages
        honeywellBTScanner.setMessageListener(new HoneywellBTScanner.OnMessageReceived() {
            @Override
            public void messageReceived(String type, ScanResult message) {
                Log.d(TAG,"Barcode Received: " + message.getBarcodeMessage());
            }

            @Override
            public void errorReceived(String type, String errorMessage) {

            }
        });
    }

    @Override
    public void onStop(){
        super.onStop();
//        btService.stop();
        honeywellBTScanner.stopScanner();
    }
}
