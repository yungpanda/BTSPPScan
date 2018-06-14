# BTSPPScan
Scanner Library for SPP Scan Devices

Use the HoneywellBTScanner class for the Honywell ringscanner.

Put the scanner in SPP Mode by scanning the BT Connection -  PDA/MObility System Device barcode
Page 1-8 of the User Guide

Usage:

@Override
public void onCreate(){
   

  honeywellBTScanner = new HoneywellBTScanner(this);
  String macAddress ="00:10:20:3E:CC:94";        
  honeywellBTScanner.initiateScanner(macAddress);
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

In onStop()
@Override
public void onStop(){
   honeywellBTScanner.stopScanner();
}
When sending commands to the scanner:
  honeywellBTScanner.sendCommand(String command);
  
  
