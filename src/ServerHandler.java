import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ServerHandler implements Runnable {

    private Socket clientSocket;
    
    public ServerHandler(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        System.out.println("Siamo nell'handler " + clientSocket.getRemoteSocketAddress());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // Leggi il messaggio dal client
                String clientMessage = reader.readLine();
                System.out.println("Messaggio ricevuto dal client: " + clientMessage);

                // Invia una risposta al client
                writer.println("Messaggio ricevuto correttamente!");

            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
