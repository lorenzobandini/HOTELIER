import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class HotelierClientMain {
 
    public static void main(String[] args) {
        GroupProperties properties = getPropertiesClient();
        
        // Stampare a video le proprietà
        System.out.println("Address: " + properties.getAddress());
        System.out.println("Port Number: " + properties.getPortNumber());

        try(Socket clientSocket = new Socket(properties.getAddress(), Integer.parseInt(properties.getPortNumber()))){
            System.out.println("Connessione stabilita con " + clientSocket.getRemoteSocketAddress());

            Thread listener = new Thread(new ClientListener(clientSocket));
            Thread writer = new Thread(new ClientWriter(clientSocket));

            listener.start();
            writer.start();

            listener.join();
            writer.join();
            
            clientSocket.close();

        }catch(ConnectException e){
            System.out.println("Server non disponibile");
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static GroupProperties getPropertiesClient() {
        Properties properties = getProperties();
        
        String socket = properties.getProperty("socket");
        String portNumber = properties.getProperty("portNumber");
        
        return new GroupProperties(socket, portNumber);
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream("client_properties.properties")) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}


class GroupProperties {

    private String address;
    private String portNumber;

    public GroupProperties(String socket, String portNumber) {
        this.address = socket;
        this.portNumber = portNumber;   
    }

    public String getAddress() {
        return address;
    }

    public String getPortNumber() {
        return portNumber;
    }
}
