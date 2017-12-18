import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by shipeng on 17-12-17.
 */
public class DebugClient {

    public static RaftRPC.Client getClient(String ip, int port) {
        try {
            TSocket sock = new TSocket(ip, port);
            TTransport transport = new TFramedTransport(sock);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            return new RaftRPC.Client(protocol);
        } catch (TTransportException e) {
            return null;
        }
    }
    public static void main(String[] args) {
        String ip = "ecelinux6.uwaterloo.ca";
        int port = 10010;
        RaftRPC.Client client = getClient(ip, port);
        System.out.println("Testing ...");
        List<String> iplist = new ArrayList<>();
        List<Integer> portlist = new ArrayList<>();
        iplist.add("ecelinux6.uwaterloo.ca");
        iplist.add("ecelinux7.uwaterloo.ca");
        iplist.add("ecelinux8.uwaterloo.ca");
        portlist.add(10010);
        portlist.add(10011);
        portlist.add(10012);
        Scanner scanner = new Scanner(System.in);
        int id = 10;
        while (true) {
            System.out.print("Enter command type (put/get) : ");
            String type = scanner.next();
            if (type.equals("put")) {
                System.out.print("Enter key : ");
                String key = scanner.next();
                System.out.print("Enter value : ");
                String value = scanner.next();
                while (true) {
                    try {
                        ClientResponse response = client.Put(id, key, value);
                        if (response.getStatus() == 0) {
                            System.out.println("Success");
                            break;
                        } else if (response.getStatus() == -1){
                            System.out.println("Change connection to " + response.getIp() + " : " + response.getPort());
                            client = getClient(response.getIp(), response.getPort());
                        }
                    } catch (TException e) {
                        while (true) {
                            boolean flag = false;
                            for (int i = 0; i < iplist.size(); i++) {
                                System.out.println("Try to connect " + i );
                                client = getClient(iplist.get(i), portlist.get(i));
                                if (client != null) {
                                    flag = true;
                                    break;
                                }
                            }
                            if (flag)
                                break;
                        }
                        //e.printStackTrace();
                    }
                }
                id += 1;
            }
            if (type.equals("get")) {
                System.out.print("Enter key : ");
                String key = scanner.next();
                while (true) {
                    try {
                        ClientResponse response = client.Get(id, key);
                        if (response.getStatus() == 0) {
                            if (response.getValue().equals("")) {
                                System.out.println("The key does not exist");
                            } else {
                                System.out.println("Results : " + response.getValue());
                            }
                            break;
                        } else if (response.getStatus() == -1){
                            System.out.println("Change connection to " + response.getIp() + " : " + response.getPort());
                            client = getClient(response.getIp(), response.getPort());
                        }
                    } catch (TException e) {
                        while (true) {
                            boolean flag = false;
                            for (int i = 0; i < iplist.size(); i++) {
                                System.out.println("Try to connect " + i );
                                client = getClient(iplist.get(i), portlist.get(i));
                                if (client != null) {
                                    flag = true;
                                    break;
                                }
                            }
                            if (flag)
                                break;
                        }
                    }
                }
            }
        }


    }
}
