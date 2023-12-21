import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ClientListener implements Runnable{

    private Socket socket;

    public ClientListener(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            // Stream per la lettura da tastiera
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

                // Stream per la scrittura al server
            OutputStream outputStream = socket.getOutputStream();

            while (true) {
                // Leggi input da tastiera
                System.out.print("Inserisci un messaggio (exit per terminare): ");
                String message = reader.readLine();

                // Invia il messaggio al server
                outputStream.write((message+"\n").getBytes());
                outputStream.flush();

                // Esci se l'utente inserisce "exit"
                if ("exit".equalsIgnoreCase(message)) {
                    break;
                }
            }
            reader.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}

