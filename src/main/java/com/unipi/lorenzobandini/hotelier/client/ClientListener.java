package com.unipi.lorenzobandini.hotelier.client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientListener implements Runnable {

    private Socket clientSocket;
    private BufferedReader in;

    /**
     * Constructs a new ClientListener with the specified client socket.
     * <p>
     * This constructor initializes the client socket and sets up a BufferedReader
     * to read input from the socket's input stream. If an IOException occurs while
     * setting up the BufferedReader, the exception's stack trace is printed.
     *
     * @param clientSocket the client socket for this ClientListener
     */
    public ClientListener(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        
        String message;

        // Continuously read input from the client socket and print it to the console
        try {
            while (!clientSocket.isClosed()) {
                try {
                    message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    System.out.println(message);
                } catch (IOException e) {
                    break;
                }
            }
        } finally {
            try {
                in.close();
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}