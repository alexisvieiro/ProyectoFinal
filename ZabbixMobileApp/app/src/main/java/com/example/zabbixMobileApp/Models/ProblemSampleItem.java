package com.example.zabbixMobileApp.Models;

public class ProblemSampleItem {
    private String sClock;
    private String sProblemName;
    private String sIsAck;
    private String sSeverity;

    public ProblemSampleItem(){}

    public ProblemSampleItem(String sClock, String sProblemName,
                             String sIsAck, String sSeverity) {
        this.sClock = sClock;
        this.sProblemName = sProblemName;
        this.sIsAck = sIsAck;
        this.sSeverity = sSeverity;
    }

    public String getsClock() {
        return sClock;
    }

    public void setsClock(String sClock) {
        this.sClock = sClock;
    }

    public String getsProblemName() {
        return sProblemName;
    }

    public void setsProblemName(String sProblemName) {
        this.sProblemName = sProblemName;
    }

    public String getsIsAck() {
        return sIsAck;
    }

    public void setsIsAck(String sIsAck) {
        this.sIsAck = sIsAck;
    }

    public String getsSeverity() {
        return sSeverity;
    }

    public void setsSeverity(String sSeverity) {
        this.sSeverity = sSeverity;
    }
}
