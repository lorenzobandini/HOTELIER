import java.net.Socket;

public class ClientWriter implements Runnable{

    private Socket clientSocket;

    public ClientWriter(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        
    }
}
