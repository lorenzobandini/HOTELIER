import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientWriter implements Runnable{

    private Socket clientSocket;
    private PrintWriter out;
    private Scanner scanner;

    public ClientWriter(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.scanner = new Scanner(System.in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Scrivi al server in continuazione
        String message;
        while (!(message = scanner.nextLine()).equals("exit")) {
            out.println(message);
        }
        out.println("exit");
        out.close();
        scanner.close();
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
