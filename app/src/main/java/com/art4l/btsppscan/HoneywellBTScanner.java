package com.art4l.btsppscan;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class HoneywellBTScanner {

    private static final String TAG = HoneywellBTScanner.class.getSimpleName();

    //Bluetooth service
    ServiceManager btService;
    private int bTRetryCounter = 0;					//retry when there are problems with Bluetooth Channel
    private static final int MAXRETRY = 10;			//retry 10 times

    private Context mContext;


    // return event
    private OnMessageReceived mMessageListener;



    public HoneywellBTScanner(Context context){
        mContext = context;

    }

    public void initiateScanner(final String macAddress){

        btService = new ServiceManager(mContext,BTServer.class, new Handler(){
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
                        ScanResult scanResult = new ScanResult();
                        scanResult.setBarcodeType("generic barcode");
                        scanResult.setBarcodeMessage((String)msg.obj);
                        mMessageListener.messageReceived("BarcodeScan",scanResult);

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
                            Log.d(TAG,"No BT Connection");
                            mMessageListener.errorReceived("NOCONNECT","Connection lost with scanner");

                            //raise an error to the application
//                            Toast.makeText(M.this,"Geen verbinding met Ringscanner!!!",Toast.LENGTH_LONG).show();
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



    }

    public void startScanner(){

        btService.start();

    }

    public void stopScanner(){
        btService.stop();
    }


    public void setMessageListener(OnMessageReceived messageListener){
        mMessageListener = messageListener;

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
            btService.send(Message.obtain(null, BTServer.WRITE_SETTING,writeOut));
        }catch(RemoteException ex) {

        }
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

}
