/**
 * Created by shipeng on 17-11-22.
 */
public class Peer {
    private String ip;
    private int port;

    public int getId() {
        return id;
    }

    private int id;

    Peer(String ip, int port, int id) {
        this.ip = ip;
        this.port = port;
        this.id = id;
    }

    String getIp() {
        return this.ip;
    }

    int getPort() {
        return this.port;
    }


}
