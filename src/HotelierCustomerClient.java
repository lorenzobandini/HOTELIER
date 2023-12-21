import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class HotelierCustomerClient {

    public static void main(String[] args) throws InterruptedException {
        String serverAddress = "localhost"; // Indirizzo del server
        int serverPort = 8080; // Porta del server

        try {
            // Connessione al server
            Socket socket = new Socket(serverAddress, serverPort);
            System.out.println("Connesso al server su " + serverAddress + ":" + serverPort);

            Thread thread = new Thread(new ClientListener(socket));
            thread.start();

            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String serverMessage;
            while ((serverMessage = serverReader.readLine()) != null) {
                System.out.println("Messaggio dal server: " + serverMessage+"\n");
                if(serverMessage.equals("exit")) {
                    break;
                }
            }
            
            // Chiudi le risorse
            socket.close();
    } catch (UnknownHostException e) {  
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
