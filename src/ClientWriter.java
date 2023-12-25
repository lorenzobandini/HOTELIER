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
        try(PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)){
            Scanner scanner = new Scanner(System.in);
            String message;
            
            while((message = scanner.nextLine()) != null){
                writer.println(message);
                if(message.equals("exit"))
                    break;
            }

            scanner.close();
            writer.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();    
        }
    }
}
