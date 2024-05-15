
public class GroupProperties {

    private String address;
    private String portNumber;
    private String multicastAddress;
    private String multicastPort;

    public GroupProperties(String socket, String portNumber, String multicastAddress, String multicastPort) {
        this.address = socket;
        this.portNumber = portNumber;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
    }

    public String getAddress() {
        return address;
    }

    public String getPortNumber() {
        return portNumber;
    }

    public String getMulticastAddress() {
        return multicastAddress;
    }

    public String getMulticastPort() {
        return multicastPort;
    }
}