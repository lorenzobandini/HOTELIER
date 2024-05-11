package com.unipi.lorenzobandini.hotelier;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;


public class HotelierClientMain {
 
    public static void main(String[] args) {
        GroupProperties properties = getPropertiesServer();
        
        int minPoolSize = properties.getMinPoolSize();
        int maxPoolSize = properties.getMaxPoolSize();
        int keepAliveTime = properties.getKeepAliveTime();

        try{
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(properties.getPortNumber()));
            ExecutorService executor = new ThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            System.out.println("Server started at address "+ properties.getAddress() +" and port " + properties.getPortNumber());
            
            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(new HotelierClientHandler(clientSocket));
                    System.out.println("Client connected at port " + clientSocket.getPort());
                }
            } finally {
                serverSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }

    private static GroupProperties getPropertiesServer() {
        Properties properties = getProperties();
        
        String socket = properties.getProperty("socket");
        String portNumber = properties.getProperty("portNumber");
        String minPoolSize = properties.getProperty("minPoolSize");
        String maxPoolSize = properties.getProperty("maxPoolSize");
        String keepAliveTime = properties.getProperty("keepAliveTime");

        return new GroupProperties(socket, portNumber, Integer.parseInt(minPoolSize), Integer.parseInt(maxPoolSize), Integer.parseInt(keepAliveTime));
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("server_properties.properties")) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}

class GroupProperties {

    private String address;
    private String portNumber;
    private int minPoolSize;
    private int maxPoolSize;
    private int keepAliveTime;

    public GroupProperties(String socket, String portNumber, int minPoolSize, int maxPoolSize, int keepAliveTime) {
        this.address = socket;
        this.portNumber = portNumber;   
        this.minPoolSize = minPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
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
}