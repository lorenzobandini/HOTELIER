import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try(Socket clientSocket = new Socket("localhost", 8080)){
            System.out.println("Connessione stabilita con " + clientSocket.getRemoteSocketAddress());

            Thread listener = new Thread(new ClientListener(clientSocket));
            Thread writer = new Thread(new ClientWriter(clientSocket));

            listener.start();
            writer.start();

            listener.join();
            writer.join();
            System.out.println("sono qui");
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
        
    }    
}
