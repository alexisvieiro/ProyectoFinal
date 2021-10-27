package com.example.esqueletoapp.Models;

public class HostSampleItem {
    private String sHostName;

    public HostSampleItem(){}

    public HostSampleItem(String sHostName) {
        this.sHostName = sHostName;
    }

    public String getsHostName() {
        return sHostName;
    }

    public void setsHostName(String sHostName) {
        this.sHostName = sHostName;
    }
}
