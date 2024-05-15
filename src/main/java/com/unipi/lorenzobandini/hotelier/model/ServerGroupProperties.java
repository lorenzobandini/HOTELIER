package com.unipi.lorenzobandini.hotelier.model;

public class ServerGroupProperties {

    private String address;
    private String portNumber;
    private String minPoolSize;
    private String maxPoolSize;
    private String keepAliveTime;
    private String timerUpdates;
    private String multicastAddress;
    private String multicastPort;

    public ServerGroupProperties(String address, String portNumber, String minPoolSize, String maxPoolSize,
            String keepAliveTime, String timerUpdates, String multicastAddress, String multicastPort) {
        this.address = address;
        this.portNumber = portNumber;
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timerUpdates = timerUpdates;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
    }

    public String getAddress() {
        return address;
    }

    public String getPortNumber() {
        return portNumber;
    }

    public String getMinPoolSize() {
        return minPoolSize;
    }

    public String getMaxPoolSize() {
        return maxPoolSize;
    }

    public String getKeepAliveTime() {
        return keepAliveTime;
    }

    public String getTimerUpdates() {
        return timerUpdates;
    }

    public String getMulticastAddress() {
        return multicastAddress;
    }

    public String getMulticastPort() {
        return multicastPort;
    }
}