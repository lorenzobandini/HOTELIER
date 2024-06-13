package com.unipi.lorenzobandini.hotelier.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import com.unipi.lorenzobandini.hotelier.model.ClientGroupProperties;

/**
 * Main class for the HotelierServer application.
 *
 * <p>
 * This class sets up the server, including creating the server socket, setting
 * up the thread pool,
 * and entering a loop to accept client connections.
 *
 * @author Lorenzo Bandini
 * @version 1.0
 * @since 2024-05-11
 * @lastUpdated 2024-05-17
 */
public class HotelierClientMain {

    public static void main(String[] args) throws InterruptedException {

        // Get the properties from the properties file
        ClientGroupProperties properties = getPropertiesClient();

        // Create the atomic boolean to check if the client is connected
        AtomicBoolean isConnected = new AtomicBoolean(true);

        // Create the executor service to manage the threads
        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {

            // Get the port number and multicast port number from the properties
            int portNumber = Integer.parseInt(properties.getPortNumber());
            int multicastPort = Integer.parseInt(properties.getMulticastPort());

            try (
                    // Create the client socket and multicast socket
                    Socket clientSocket = new Socket(properties.getAddress(), portNumber);
                    MulticastSocket multicastSocket = new MulticastSocket(multicastPort)) {

                // Join the multicast group
                InetAddress group = InetAddress.getByName(properties.getMulticastAddress());
                SocketAddress socketAddress = new InetSocketAddress(group, multicastSocket.getLocalPort());
                multicastSocket.joinGroup(socketAddress, null);

                // Create the threads for the client listener, client writer, and multicast
                // listener
                Thread clientListenerThread = new Thread(new ClientListener(clientSocket));
                Thread clientWriterThread = new Thread(new ClientWriter(clientSocket, isConnected));
                Thread multicastListenerThread = new Thread(
                        new ClientMulticastListener(multicastSocket, isConnected));

                try {

                    // Start the threads
                    clientListenerThread.start();
                    clientWriterThread.start();
                    multicastListenerThread.start();

                    // Wait for the threads to finish
                    clientListenerThread.join();
                    clientWriterThread.join();
                    multicastListenerThread.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                System.out.println("Error: Unable to establish connection.");
                e.printStackTrace();
            } finally {
                executor.shutdown();
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Port number must be an integer.");
        }
    }

    /**
     * Retrieves client properties from a configuration file.
     * <p>
     * This method reads a configuration file and retrieves the following
     * properties:
     * <ul>
     * <li>Socket address</li>
     * <li>Port number</li>
     * <li>Multicast address</li>
     * <li>Multicast port</li>
     * </ul>
     * 
     * @return A GroupProperties object containing the retrieved properties.
     */
    private static ClientGroupProperties getPropertiesClient() {
        Properties properties = getProperties();

        String socket = properties.getProperty("address");
        String portNumber = properties.getProperty("portNumber");
        String multicastAddress = properties.getProperty("multicastAddress");
        String multicastPort = properties.getProperty("multicastPort");

        return new ClientGroupProperties(socket, portNumber, multicastAddress, multicastPort);
    }

    /**
     * Loads and returns properties from the client_properties.properties file.
     * <p>
     * This method attempts to open a FileInputStream on the
     * client_properties.properties file,
     * and loads the properties from this file into a Properties object.
     * If an IOException occurs during this process, it is caught and its stack
     * trace is printed.
     * 
     * @return A Properties object containing the properties loaded from the file.
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