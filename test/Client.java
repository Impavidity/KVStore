import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.Scanner;
import java.util.UUID;

/**
 * Created by shipeng on 17-11-26.
 */
public class Client {

    public static RaftRPC.Client getClient(String ip, int port) {
        while (true) {
            try {
                TSocket sock = new TSocket(ip, port);
                TTransport transport = new TFramedTransport(sock);
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                return new RaftRPC.Client(protocol);
            } catch (TTransportException e) {
                //e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }
    public static void main(String[] args) {
        String ip = "ecelinux6.uwaterloo.ca";
        int port = 10010;
        RaftRPC.Client client = getClient(ip, port);
        System.out.println("Testing ...");
        for (int i=0; i<10; i++) {
            String key = UUID.randomUUID().toString();
            String value = UUID.randomUUID().toString();
            while (true) {
                try {
                    ClientResponse response = client.Put(i, key, value);
                    if (response.getStatus() == 0) {
                        System.out.println("Put " + key + " : " + value + " successfully");
                        break;
                    } else if (response.getStatus() == -1) {
                        System.out.println("Change connection to " + response.getIp() +  " : " + response.getPort());
                        client = getClient(response.getIp(), response.getPort());
                    }
                } catch (TException e) {
                    //e.printStackTrace();
                }
            }
            while (true) {
                try {
                    ClientResponse response = client.Get(i, key);
                    if (response.getStatus() == 0) {
                        if (response.getValue().equals(value)) {
                            System.out.println("Get " + key + " : " + response.getValue() + " Correct");
                        } else {
                            System.out.println("Get " + key + " : " + response.getValue() +
                                    " Wrong. The right value should be " + value);
                        }
                        break;
                    } else if (response.getStatus() == -1) {
                        System.out.println("Change connection to " + response.getIp() +  " : " + response.getPort());
                        client = getClient(response.getIp(), response.getPort());
                    }
                } catch (TException e) {
                    //e.printStackTrace();
                }
            }
        }
        System.out.println("Finish Testing ...\n Enter Command line mode:");
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
                        //e.printStackTrace();
                    }
                }
            }
        }


    }
}
