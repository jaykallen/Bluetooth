package com.procatdt.appbluetooth;

// Created by Jay Kallen on 10/13/2017:

public class BluetoothModel {
    private static BluetoothModel mInstance = null;
    private String name;
    private String macAddress;
    private boolean isPaired;
    private boolean isSelected;

    public static BluetoothModel getInstance(){
        if(mInstance == null) {
            mInstance = new BluetoothModel();
        }
        return mInstance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isPaired() {
        return isPaired;
    }

    public void setPaired(boolean paired) {
        isPaired = paired;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
