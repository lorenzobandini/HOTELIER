import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MulticastListener implements Runnable {
    private MulticastSocket multicastSocket;
    private AtomicBoolean isConnected;

    /**
     * Constructs a new MulticastListener with the specified multicast socket and
     * connection status.
     * <p>
     * This constructor initializes the multicast socket and sets the connection
     * status of this MulticastListener.
     *
     * @param multicastSocket the multicast socket for this MulticastListener
     * @param isConnected     the connection status for this MulticastListener
     */
    public MulticastListener(MulticastSocket multicastSocket, AtomicBoolean isConnected) {
        this.multicastSocket = multicastSocket;
        this.isConnected = isConnected;
    }

    @Override
    public void run() {

        byte[] buffer = new byte[256];

        try {

            // If the multicast socket doesn't receive any packets for 500 milliseconds, disconnect
            this.multicastSocket.setSoTimeout(500);

            // Continuously receive packets from the multicast socket and print them to the console until the client disconnects
            while (isConnected.get()) {

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());

                if (!message.equals("Awake")) {
                    System.out.println(message);
                }

                // Clear the buffer
                java.util.Arrays.fill(buffer, (byte) 0);
            }
        } catch (SocketTimeoutException e) {
            System.err.println("Timeout while waiting for packet");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error while receiving packet");
            e.printStackTrace();
        }
    }
}