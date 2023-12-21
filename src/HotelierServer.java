import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HotelierServer {

    private static final int port = 8080;

    public static void main(String[] args) {
        

        try (ServerSocket serverSocket = new ServerSocket(port, 0, InetAddress.getByName("127.0.0.1"));){
            System.out.println("Server in ascolto sulla porta " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuova connessione da " + clientSocket.getInetAddress().getHostAddress());

                Thread thread = new Thread(new ClientHandler(clientSocket));
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
