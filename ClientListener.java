import java.net.Socket;

public class ClientListener implements Runnable{

    private Socket clientSocket;

    public ClientListener(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        
    }
}
