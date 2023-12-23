import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class Server {

    
    public static void main(String[] args) throws InterruptedException {

        int portaServer = 8080;
        int minPoolSize = 5;
        int maxPoolSize = 15;
        int keepAliveTime = 15;

        try(ServerSocket serverSocket = new ServerSocket(portaServer);
            ExecutorService executor = new ThreadPoolExecutor(minPoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());)
            {
            System.out.println("Server in ascolto sulla porta " + portaServer);

            int i = 0;

            while(i < 10){
                Socket clientSocket = serverSocket.accept();
                System.out.println("Connessione accettata da " + clientSocket.getRemoteSocketAddress());
                executor.execute(new ServerHandler(clientSocket));
                i++;

            }

            executor.shutdown();
            while(!executor.isTerminated()){
                Thread.sleep(100);
            }
            serverSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
