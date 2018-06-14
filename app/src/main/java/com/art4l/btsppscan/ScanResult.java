package com.art4l.btsppscan;

/**
 * Object to store the result of a Scan read action from a scanner
 *
 */

public class ScanResult {

    private String barcodeType;
    private String barcodeMessage;

    public ScanResult(){


    }


    public String getBarcodeType() {
        return barcodeType;
    }

    public void setBarcodeType(String barcodeType) {
        this.barcodeType = barcodeType;
    }

    public String getBarcodeMessage() {
        return barcodeMessage;
    }

    public void setBarcodeMessage(String barcodeMessage) {
        this.barcodeMessage = barcodeMessage;
    }
}
