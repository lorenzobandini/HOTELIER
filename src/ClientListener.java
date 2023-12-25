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

            do{
                System.out.println("> "+ serverMessage);
                if(serverMessage.equals("exit")){
                    break;
                }
            }while((serverMessage = reader.readLine()) != null);

            reader.close();
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();    
        }
        

    }
}
