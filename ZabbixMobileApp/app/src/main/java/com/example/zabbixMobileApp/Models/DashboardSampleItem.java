package com.example.zabbixMobileApp.Models;

public class DashboardSampleItem {
    private String sItemName;
    private String sHostname;

    public DashboardSampleItem(){}

    public DashboardSampleItem(String sItemName, String sHostname) {
        this.sItemName = sItemName;
        this.sHostname = sHostname;
    }

    public String getsItemName() {
        return sItemName;
    }

    public String getsHostname() {
        return sHostname;
    }

    public void setsItemName(String sItemName) {
        this.sItemName = sItemName;
    }

    public void setsHostname(String sHostname) {
        this.sHostname = sHostname;
    }
}
