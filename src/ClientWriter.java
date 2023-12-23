import java.io.PrintWriter;
import java.net.Socket;

public class ClientWriter implements Runnable {

    private Socket clientSocket;
    
    public ClientWriter(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        System.out.println("Siamo nel writer di: " + clientSocket.getRemoteSocketAddress());
        try(PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)){
            writer.println("Messaggio ricevuto correttamente!");
        } catch (Exception e) {
            e.printStackTrace();    
        }

    }
}
