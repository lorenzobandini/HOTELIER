import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

class ClientHandler implements Runnable {

    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            // Ottieni gli stream di input e output dal client
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream outputStream = clientSocket.getOutputStream();

            // Loop per ascoltare i messaggi dal client
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("Messaggio ricevuto da " + clientSocket.getInetAddress().getHostAddress() + ": " + message);
                
                outputStream.write(("Tu sei: "+ clientSocket.getInetAddress().getHostAddress()).getBytes());
                outputStream.flush();
            }

            // Chiudi la connessione quando il client si disconnette
            System.out.println("Connessione chiusa da " + clientSocket.getInetAddress().getHostAddress());
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
