import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientWriter implements Runnable {

    private Socket clientSocket;
    private PrintWriter out;
    private Scanner scanner;
    private AtomicBoolean isConnected;

    public ClientWriter(Socket clientSocket, AtomicBoolean isConnected) {
        this.clientSocket = clientSocket;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.scanner = new Scanner(System.in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.isConnected = isConnected;
    }

    @Override
    public void run() {

        String message;
        while (scanner.hasNextLine() && !(message = scanner.nextLine()).equals("exit")) {
            out.println(message);
        }
        out.println("exit");
        out.close();
        scanner.close();
        isConnected.set(false);
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
