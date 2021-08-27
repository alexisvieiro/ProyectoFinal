package com.example.esqueletoapp.Models;

public class DeviceSampleItem {
    private String sDeviceName;

    public DeviceSampleItem(){}

    public DeviceSampleItem(String sDeviceName) {
        this.sDeviceName = sDeviceName;
    }

    public String getsDeviceName() {
        return sDeviceName;
    }

    public void setsDeviceName(String sDeviceName) {
        this.sDeviceName = sDeviceName;
    }
}
