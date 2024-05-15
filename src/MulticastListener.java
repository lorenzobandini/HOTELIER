import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MulticastListener implements Runnable {
    private MulticastSocket multicastSocket;
    private AtomicBoolean isConnected;

    public MulticastListener(MulticastSocket multicastSocket, AtomicBoolean isConnected) {
        this.multicastSocket = multicastSocket;
        this.isConnected = isConnected;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[256];

        try {
            this.multicastSocket.setSoTimeout(500);
            while (isConnected.get()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                if (!message.equals("Awake")) {
                    System.out.println(message);
                }
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