import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by shipeng on 17-12-17.
 */
public class DebugClient {

    static InetSocketAddress primaryAddress;
    static List<String> iplist = new ArrayList<>();
    static List<Integer> portlist = new ArrayList<>();

    static InetSocketAddress getPrimaryAddress() {
        Random rand = new Random();
        int index = Math.abs(rand.nextInt()) % 3;
        return new InetSocketAddress(iplist.get(index), portlist.get(index));
    }

    static RaftRPC.Client getThriftClient() {

        while (true) {
            try {
                TSocket sock = new TSocket(primaryAddress.getHostName(), primaryAddress.getPort());
                TTransport transport = new TFramedTransport(sock);
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                System.out.println("Connecting to " + primaryAddress.getHostName());
                return new RaftRPC.Client(protocol);
            } catch (TTransportException e) {
                //System.out.println("Unable to connect to primary\n" + e);
                primaryAddress = getPrimaryAddress();

            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {}
        }
    }


    public static void main(String[] args) {
        iplist.add("ecelinux6.uwaterloo.ca");
        iplist.add("ecelinux7.uwaterloo.ca");
        iplist.add("ecelinux8.uwaterloo.ca");
        portlist.add(10010);
        portlist.add(10011);
        portlist.add(10012);
        primaryAddress = getPrimaryAddress();
        RaftRPC.Client client = getThriftClient();
        System.out.println("Testing ...");
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
                            primaryAddress = new InetSocketAddress(response.getIp(), response.getPort());
                            client = getThriftClient();
                        }
                    } catch (TException e) {
                        client = getThriftClient();
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
                            primaryAddress = new InetSocketAddress(response.getIp(), response.getPort());
                            client = getThriftClient();
                        }
                    } catch (TException e) {
                        client = getThriftClient();

                    }
                }
            }
        }


    }
}
