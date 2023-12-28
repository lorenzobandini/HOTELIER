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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))){
            String serverMessage;

            serverMessage = reader.readLine();

            while ((serverMessage = reader.readLine()) != null) {
                System.out.println("> " + serverMessage);
                if (serverMessage.equals("exit")) {
                    break;
                }
                
                if (clientSocket.isClosed()) {
                    // Esci dal loop se il socket è chiuso
                    break;
                }
            }
            
            reader.close();
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();    
        }
        

    }
}
