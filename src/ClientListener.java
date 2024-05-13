import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientListener implements Runnable{

    private Socket clientSocket;
    private BufferedReader in;

    public ClientListener(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // Leggi dal server in continuazione
        String message;
        try {
            while (!clientSocket.isClosed()) {
                try {
                    message = in.readLine();
                    if (message == null) {
                        break;
                    }
                    System.out.println(message);
                } catch (IOException e) {
                    // Il BufferedReader è stato chiuso
                    break;
                }
            }
        } finally {
            try {
                in.close();
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}