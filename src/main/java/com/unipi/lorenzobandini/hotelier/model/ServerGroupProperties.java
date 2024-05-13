package com.unipi.lorenzobandini.hotelier.model;

public class ServerGroupProperties {

    private String address;
    private String portNumber;
    private int minPoolSize;
    private int maxPoolSize;
    private int keepAliveTime;
    private int timerUpdates;

    public ServerGroupProperties(String socket, String portNumber, int minPoolSize, int maxPoolSize, int keepAliveTime, int timerUpdates) {
        this.address = socket;
        this.portNumber = portNumber;   
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timerUpdates = timerUpdates;
    }

    public String getAddress() {
        return address;
    }

    public String getPortNumber() {
        return portNumber;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    public int getTimerUpdates() {
        return timerUpdates;
    }
}