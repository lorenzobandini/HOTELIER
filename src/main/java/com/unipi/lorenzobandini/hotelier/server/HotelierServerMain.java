package com.unipi.lorenzobandini.hotelier.server;

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

/**
 * Main class for the HotelierServer application.
 *
 * <p>This class sets up the server, including creating the server socket, setting up the thread pool,
 * and entering a loop to accept client connections.
 *
 * @author Lorenzo Bandini
 * @version 1.0
 * @since 2024-05-11
 * @lastUpdated 2024-05-17
 */
public class HotelierServerMain {

    public static void main(String[] args) {

        // Create a struct that contains all the properties of the server
        ServerGroupProperties properties = getPropertiesServer();

        int minPoolSize = Integer.parseInt(properties.getMinPoolSize());
        int maxPoolSize = Integer.parseInt(properties.getMaxPoolSize());
        int keepAliveTime = Integer.parseInt(properties.getKeepAliveTime());
        int timerUpdates = Integer.parseInt(properties.getTimerUpdates());

        try {

            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(properties.getPortNumber()), 0,
                    InetAddress.getByName(properties.getAddress()));

            // Create a multicast socket
            MulticastSocket multicastSocket = new MulticastSocket(Integer.parseInt(properties.getMulticastPort()));

            // Create a thread pool for handling clients
            ExecutorService executor = new ThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>());

            System.out.println(
                    "Server started at address " + properties.getAddress() + " and port " + properties.getPortNumber());
            System.out.println("Server multicast started at address " + properties.getMulticastAddress() + " and port "
                    + properties.getMulticastPort());

            // Create locks for synconous access to the hotels and reviews by chartThread
            // and clientHandlers
            final Object lockHotels = new Object();
            final Object lockReviews = new Object();

            // Create a Gson object with custom serializers and deserializers for LocalDate
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(LocalDate.class,
                            (JsonSerializer<LocalDate>) (src, typeOfSrc,
                                    context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                    .registerTypeAdapter(LocalDate.class,
                            (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> LocalDate
                                    .parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                    .create();

            // Create a thread that sends a multicast message every 250 milliseconds
            new Thread(new HotelierUpdaterChart(timerUpdates, gson, multicastSocket,
                    Integer.parseInt(properties.getMulticastPort()), properties.getMulticastAddress(), lockHotels,
                    lockReviews))
                    .start();

            // Create a thread that listens for multicast messages
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

    /**
     * Retrieves the server properties from a Properties object.
     * <p>
     * This method reads the following properties:
     * <ul>
     * <li>address: the server's address</li>
     * <li>portNumber: the server's port number</li>
     * <li>minPoolSize: the minimum size of the server's thread pool</li>
     * <li>maxPoolSize: the maximum size of the server's thread pool</li>
     * <li>keepAliveTime: the keep-alive time for inactive threads in the pool</li>
     * <li>timerUpdates: the interval between server updates</li>
     * <li>multicastAddress: the server's multicast address</li>
     * <li>multicastPort: the server's multicast port</li>
     * </ul>
     * <p>
     * The properties are then used to create and return a new ServerGroupProperties
     * object.
     * 
     * @return a new ServerGroupProperties object containing the server properties
     */
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

    /**
     * Retrieves the server properties from the "server_properties.properties" file.
     *
     * <p>
     * This method attempts to load the properties from a file named
     * "server_properties.properties".
     * If an IOException occurs during this process, the stack trace is printed.
     * 
     * @return a Properties object containing the server properties loaded from the
     *         file
     */
    private static Properties getProperties() {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("properties.properties")) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}