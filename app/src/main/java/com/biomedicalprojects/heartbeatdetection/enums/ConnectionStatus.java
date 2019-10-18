package com.biomedicalprojects.heartbeatdetection.enums;

public enum ConnectionStatus {
    CONNECT("CONNECT",1),
    DISCONNECT("DISCONNECT",2),
    LOADING("LOADING",0);

    private String key;
    private int value;

    ConnectionStatus(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
