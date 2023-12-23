import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientWriter implements Runnable {

    private Socket clientSocket;
    
    public ClientWriter(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        System.out.println("Siamo nel writer di: " + clientSocket.getRemoteSocketAddress());

        try(PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)){
            Scanner scanner = new Scanner(System.in);
            String message;
            while((message = scanner.nextLine()) != null){
                writer.println(message);
                if(message.equals("exit"))
                    break;
            }
            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();    
        }

    }
}
