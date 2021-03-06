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
import java.util.UUID;

/**
 * Created by shipeng on 17-11-26.
 */
public class Client {

    public static RaftRPC.Client getClient(String ip, int port) {
//        while (true) {
            try {
                TSocket sock = new TSocket(ip, port);
                TTransport transport = new TFramedTransport(sock);
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                return new RaftRPC.Client(protocol);
            } catch (TTransportException e) {
                //e.printStackTrace();
                return null;
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
            }
//        }

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
        while (true) {
            boolean flag = false;
            for (int i = 0; i < iplist.size(); i++) {
                client = getClient(iplist.get(i), portlist.get(i));
                if (client != null) {
                    flag = true;
                    break;
                }
            }
            if (flag)
                break;
        }

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



    }
}
