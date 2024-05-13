package com.unipi.lorenzobandini.hotelier;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.ExecutorService;


public class HotelierServerMain {
 
    public static void main(String[] args) {
        ServerGroupProperties properties = getPropertiesServer();
        
        int minPoolSize = properties.getMinPoolSize();
        int maxPoolSize = properties.getMaxPoolSize();
        int keepAliveTime = properties.getKeepAliveTime();
        int timerUpdates = properties.getTimerUpdates();
        try{
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(properties.getPortNumber()));
            ExecutorService executor = new ThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
            System.out.println("Server started at address "+ properties.getAddress() +" and port " + properties.getPortNumber());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            //executor.submit(new HotelierUpdaterChart(timerUpdates, gson));

            try {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(new HotelierClientHandler(clientSocket, gson));
                    System.out.println("Client connected at port " + clientSocket.getPort());
                }
            } finally {
                serverSocket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }

    private static ServerGroupProperties getPropertiesServer() {
        Properties properties = getProperties();
        
        String socket = properties.getProperty("socket");
        String portNumber = properties.getProperty("portNumber");
        String minPoolSize = properties.getProperty("minPoolSize");
        String maxPoolSize = properties.getProperty("maxPoolSize");
        String keepAliveTime = properties.getProperty("keepAliveTime");
        String timerUpdates = properties.getProperty("timerUpdates");

        return new ServerGroupProperties(socket, portNumber, Integer.parseInt(minPoolSize), Integer.parseInt(maxPoolSize), Integer.parseInt(keepAliveTime), Integer.parseInt(timerUpdates));
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