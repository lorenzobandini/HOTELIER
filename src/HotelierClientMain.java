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

public class HotelierClientMain {

    public static void main(String[] args) throws InterruptedException {
        GroupProperties properties = getPropertiesClient();

        AtomicBoolean isConnected = new AtomicBoolean(true);
        ExecutorService executor = Executors.newFixedThreadPool(3);

        try {
            int portNumber = Integer.parseInt(properties.getPortNumber());
            int multicastPort = Integer.parseInt(properties.getMulticastPort());

            try (Socket clientSocket = new Socket(properties.getAddress(), portNumber);
                    MulticastSocket multicastSocket = new MulticastSocket(multicastPort)) {
                InetAddress group = InetAddress.getByName(properties.getMulticastAddress());
                SocketAddress socketAddress = new InetSocketAddress(group, multicastSocket.getLocalPort());
                multicastSocket.joinGroup(socketAddress, null);

                Thread clientListenerThread = new Thread(new ClientListener(clientSocket));
                Thread clientWriterThread = new Thread(new ClientWriter(clientSocket, isConnected));
                Thread multicastListenerThread = new Thread(
                        new MulticastListener(multicastSocket, isConnected));

                try {
                    clientListenerThread.start();
                    clientWriterThread.start();
                    multicastListenerThread.start();

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

    private static GroupProperties getPropertiesClient() {
        Properties properties = getProperties();

        String socket = properties.getProperty("address");
        String portNumber = properties.getProperty("portNumber");
        String multicastAddress = properties.getProperty("multicastAddress");
        String multicastPort = properties.getProperty("multicastPort");

        return new GroupProperties(socket, portNumber, multicastAddress, multicastPort);
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