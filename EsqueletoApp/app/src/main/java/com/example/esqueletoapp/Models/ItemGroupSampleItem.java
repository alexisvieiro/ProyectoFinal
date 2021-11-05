package com.example.esqueletoapp.Models;

public class ItemGroupSampleItem {
    private String sAppName;
    private String sHostID;

    public ItemGroupSampleItem(){}

    public ItemGroupSampleItem(String sAppName, String sHostID) {
        this.sAppName = sAppName;
        this.sHostID = sHostID;
    }

    public String getsAppName() {
        return sAppName;
    }

    public void setsAppName(String sAppName) {
        this.sAppName = sAppName;
    }

    public String getsHostID() {
        return sHostID;
    }

    public void setsHostID(String sHostID) {
        this.sHostID = sHostID;
    }
}
