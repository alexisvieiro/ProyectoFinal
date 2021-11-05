package com.example.esqueletoapp.Models;

public class ItemSampleItem {
    private String sItemName;
    private String sLastValue;
    private String sItemUnits;
    private String sLastCheck;
    private String sDescription;

    public ItemSampleItem(){}

    public ItemSampleItem(String sItemName, String sLastValue, String sItemUnits,
                          String sLastCheck, String sDescription) {
        this.sItemName = sItemName;
        this.sLastValue = sLastValue;
        this.sItemUnits = sItemUnits;
        this.sLastCheck = sLastCheck;
        this.sDescription = sDescription;
    }

    public String getsItemName() {
        return sItemName;
    }

    public void setsItemName(String sItemName) {
        this.sItemName = sItemName;
    }

    public String getsLastValue() {
        return sLastValue;
    }

    public void setsLastValue(String sLastValue) {
        this.sLastValue = sLastValue;
    }

    public String getsItemUnits() {
        return sItemUnits;
    }

    public void setsItemUnits(String sItemUnits) {
        this.sItemUnits = sItemUnits;
    }

    public String getsLastCheck() {
        return sLastCheck;
    }

    public void setsLastCheck(String sLastCheck) {
        this.sLastCheck = sLastCheck;
    }

    public String getsDescription() {
        return sDescription;
    }

    public void setsDescription(String sDescription) {
        this.sDescription = sDescription;
    }
}
