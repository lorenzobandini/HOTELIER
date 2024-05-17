package com.unipi.lorenzobandini.hotelier;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import com.unipi.lorenzobandini.hotelier.model.ServerGroupProperties;

import java.util.concurrent.ExecutorService;

public class HotelierServerMain {
    // TODO: vedere se posso accorpare dei metodi usando delle funzioni. Rivedere algoritmo di punteggio. Fare commenti e documentazione su codice. Creare file .jar. Unire le repository e vedere come eseguirle insieme.
    public static void main(String[] args) {
        ServerGroupProperties properties = getPropertiesServer();

        int minPoolSize = Integer.parseInt(properties.getMinPoolSize());
        int maxPoolSize = Integer.parseInt(properties.getMaxPoolSize());
        int keepAliveTime = Integer.parseInt(properties.getKeepAliveTime());
        int timerUpdates = Integer.parseInt(properties.getTimerUpdates());
        try {
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(properties.getPortNumber()), 0,
                    InetAddress.getByName(properties.getAddress()));

            MulticastSocket multicastSocket = new MulticastSocket(Integer.parseInt(properties.getMulticastPort()));

            ExecutorService executor = new ThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());

            System.out.println(
                    "Server started at address " + properties.getAddress() + " and port " + properties.getPortNumber());
            System.out.println("Server multicast started at address " + properties.getMulticastAddress() + " and port "
                    + properties.getMulticastPort());
            final Object lockHotels = new Object();
            final Object lockReviews = new Object();
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(LocalDate.class,
                            (JsonSerializer<LocalDate>) (src, typeOfSrc,
                                    context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                    .registerTypeAdapter(LocalDate.class,
                            (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate
                                    .parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                    .create();

            new Thread(new HotelierUpdaterChart(timerUpdates, gson, multicastSocket,
                    Integer.parseInt(properties.getMulticastPort()), properties.getMulticastAddress(), lockHotels,
                    lockReviews))
                    .start();

            new Thread(() -> {
                try {
                    while (true) {
                        Socket clientSocket = serverSocket.accept();
                        executor.submit(new HotelierClientHandler(clientSocket, gson, lockHotels, lockReviews));
                        System.out.println("Client connected at port " + clientSocket.getPort());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static ServerGroupProperties getPropertiesServer() {
        Properties properties = getProperties();

        String address = properties.getProperty("address");
        String portNumber = properties.getProperty("portNumber");
        String minPoolSize = properties.getProperty("minPoolSize");
        String maxPoolSize = properties.getProperty("maxPoolSize");
        String keepAliveTime = properties.getProperty("keepAliveTime");
        String timerUpdates = properties.getProperty("timerUpdates");
        String multicastAddress = properties.getProperty("multicastAddress");
        String multicastPort = properties.getProperty("multicastPort");

        return new ServerGroupProperties(address, portNumber, minPoolSize, maxPoolSize, keepAliveTime, timerUpdates,
                multicastAddress, multicastPort);
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