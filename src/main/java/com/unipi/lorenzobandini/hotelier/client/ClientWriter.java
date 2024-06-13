package com.unipi.lorenzobandini.hotelier.client;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientWriter implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;
    private Scanner scanner;
    private AtomicBoolean isConnected;

    /**
     * Constructs a new ClientWriter with the specified client socket and connection
     * status.
     * <p>
     * This constructor initializes the client socket, sets up a PrintWriter to
     * write output
     * to the socket's output stream, and a Scanner to read input from the system's
     * input stream.
     * If an IOException occurs while setting up the PrintWriter, the exception's
     * stack trace is printed.
     * It also sets the connection status of this ClientWriter.
     *
     * @param clientSocket the client socket for this ClientWriter
     * @param isConnected  the connection status for this ClientWriter
     */
    public ClientWriter(Socket clientSocket, AtomicBoolean isConnected) {
        this.clientSocket = clientSocket;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.scanner = new Scanner(System.in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.isConnected = isConnected;
    }

    @Override
    public void run() {

        String message;

        // Continuously read input from the console and write it to the client socket
        // until the client writes "exit"
        while (scanner.hasNextLine() && !(message = scanner.nextLine()).equals("exit")) {
            out.println(message);
        }
        out.println("exit");
        out.close();
        scanner.close();
        isConnected.set(false);
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
