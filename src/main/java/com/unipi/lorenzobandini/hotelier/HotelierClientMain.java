package com.unipi.lorenzobandini.hotelier;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class HotelierClientMain {
 
    public static void main(String[] args) {
        GroupProperties properties = getPropertiesClient();
        
        // Stampare a video le proprietà
        System.out.println("Address: " + properties.getAddress());
        System.out.println("Port Number: " + properties.getPortNumber());

        
    }

    private static GroupProperties getPropertiesClient() {
        Properties properties = getProperties();
        
        String socket = properties.getProperty("socket");
        String portNumber = properties.getProperty("portNumber");
        
        return new GroupProperties(socket, portNumber);
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("client_properties.properties")) {
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

    public GroupProperties(String socket, String portNumber) {
        this.address = socket;
        this.portNumber = portNumber;   
    }

    public String getAddress() {
        return address;
    }

    public String getPortNumber() {
        return portNumber;
    }
}