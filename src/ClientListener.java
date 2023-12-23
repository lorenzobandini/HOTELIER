import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
public class ClientListener implements Runnable {

    private Socket clientSocket;
    
    public ClientListener(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        System.out.println("Siamo nel listener di: " + clientSocket.getRemoteSocketAddress());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))){
            String serverMessage;
            while((serverMessage = reader.readLine()) != null){
                System.out.println("Messaggio ricevuto dal server: " + serverMessage);
                if(serverMessage.equals("exit")){
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();    
        }
        

    }
}
