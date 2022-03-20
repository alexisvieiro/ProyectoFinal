package com.example.zabbixMobileApp.Models;

public class HostSampleItem {
    private String sHostName;
    private String sHostID;

    public HostSampleItem(){}

    public HostSampleItem(String sHostName, String sHostID) {
        this.sHostName = sHostName;
        this.sHostID = sHostID;
    }

    public String getsHostName() {
        return sHostName;
    }

    public void setsHostName(String sHostName) {
        this.sHostName = sHostName;
    }

    public String getsHostID() {
        return sHostID;
    }

    public void setsHostID(String sHostID) {
        this.sHostID = sHostID;
    }
}
