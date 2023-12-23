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
                String clientMessage;

                while((clientMessage = reader.readLine()) != null){
                    System.out.println("Messaggio ricevuto dal client: " + clientMessage);

                    writer.println(clientMessage);
                    if(clientMessage.equals("exit")){
                        break;
                    }
                }
                

            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
