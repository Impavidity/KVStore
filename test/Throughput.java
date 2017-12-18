import ca.uwaterloo.watca.ExecutionLogger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by shipeng on 17-12-17.
 */
public class Throughput {
    int numThreads;
    int numSeconds;
    int keyspaceSize;
    AtomicInteger globalNumOps;
    volatile boolean done = false;
    ExecutionLogger exlog;
    BufferedWriter writer = new BufferedWriter(new FileWriter("Time.txt"));
    volatile InetSocketAddress primaryAddress;
    static List<String> iplist = new ArrayList<>();
    static List<Integer> portlist = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println(args.length);
            System.err.println("Usage: java Throughput num_threads num_second keyspace_size");
            System.exit(-1);
        }

        iplist.add("ecelinux6.uwaterloo.ca");
        iplist.add("ecelinux7.uwaterloo.ca");
        iplist.add("ecelinux8.uwaterloo.ca");
        portlist.add(10010);
        portlist.add(10011);
        portlist.add(10012);

        Throughput throughput = new Throughput(Integer.parseInt(args[0]),
                                                       Integer.parseInt(args[1]),
                                                       Integer.parseInt(args[2]));
        try {
            throughput.start();
            throughput.execute();
        } catch (Exception e) {
            System.out.println("Uncaught exception\n" + e);
        } finally {
            throughput.stop();
        }

    }

    Throughput(int numThreads, int numSeconds, int keyspaceSize) throws IOException {
        this.numThreads = numThreads;
        this.numSeconds = numSeconds;
        this.keyspaceSize = keyspaceSize;
        globalNumOps = new AtomicInteger();
        primaryAddress = null;
        exlog = new ExecutionLogger("execution.log");
    }

    RaftRPC.Client getThriftClient() {

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

    InetSocketAddress getPrimaryAddress() {
        Random rand = new Random();
        int index = Math.abs(rand.nextInt()) % 3;
        return new InetSocketAddress(iplist.get(index), portlist.get(index));
    }

    void start() {
        exlog.start();
    }

    void execute() throws Exception {
        primaryAddress = getPrimaryAddress();
        List<Thread> tlist = new ArrayList<>();
        List<MyRunnable> rlist = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            MyRunnable r = new MyRunnable();
            Thread t = new Thread(r);
            tlist.add(t);
            rlist.add(r);
        }
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            tlist.get(i).start();
        }
        System.out.println("Done Starting " + numThreads + " threads");
        Thread.sleep(numSeconds * 1000);
        done = true;
        for (Thread t : tlist) {
            t.join();
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        int tput = (int)(1000f * globalNumOps.get() / estimatedTime);
        System.out.println("Aggregate throughput: " + tput + " RPCs/s");
        long totalLatency = 0;
        for (MyRunnable r: rlist) {
            totalLatency += r.getTotalTime();
        }
        double avgLatency = (double)totalLatency / globalNumOps.get() / 1000;
        System.out.println("Average latency: " + ((int)(avgLatency*100))/100f + " ms");
        System.out.println("Operations number from client: " + globalNumOps.get());
        RaftRPC.Client client = getThriftClient();
        TimeResponse response = client.getTime();
        System.out.println("Operations number from server: " + response.getOperations());
        System.out.println("Operations into execute: " + response.getInexecs());
        System.out.println("Time for mutex: " + (double)response.getMutex() / response.getOperations() / 1000000);
        System.out.println("Time for statemachine: " + (double)response.getStatemachine() / response.getOperations() / 1000000);
        System.out.println("Period Ops: " + response.getPeriodops());
        System.out.println("Time for updateindex: " + (double)response.getUpdateindex() / response.getPeriodops() / 1000000);
        System.out.println("Time for updatepeers: " + (double)response.getUpdatepeers() / response.getPeriodops() / 1000000);
        writer.close();

    }

    void stop() {
        exlog.stop();
    }

    class MyRunnable implements Runnable {
        long totalTime;
        RaftRPC.Client client;
        MyRunnable() throws TException {
            client = getThriftClient();
        }

        long getTotalTime() { return totalTime; }

        public void run() {
            Random rand = new Random();
            totalTime = 0;
            long tid = Thread.currentThread().getId();
            int numOps = 0;
            try {
                while (!done) {
                    long startTime = System.nanoTime();
                    boolean type = false;
                    if (type=rand.nextBoolean()) {
                        while (true) {
                            String key = "key-" + (Math.abs(rand.nextLong()) % keyspaceSize);
                            String value = "value-" + Math.abs(rand.nextLong());
                            exlog.logWriteInvocation(tid, key, value);
                            ClientResponse resp = client.Put(-1, key, value);
                            if (resp.getStatus() == 0) {
                                exlog.logWriteResponse(tid, key);
                                numOps++;
                                break;
                            } else if (resp.getStatus() == -1) {
                                System.out.println("Redirect to " + resp.getIp());
                                primaryAddress = new InetSocketAddress(resp.getIp(), resp.getPort());
                                client = getThriftClient();
                            } else {
                                System.out.println("No leader currently");
                            }
                        }
                    } else {
                        while (true) {
                            String key = "key-" + (Math.abs(rand.nextLong()) % keyspaceSize);
                            exlog.logReadInvocation(tid, key);
                            ClientResponse resp = client.Get(-1, key);
                            if (resp.getStatus() == 0) {
                                exlog.logReadResponse(tid, key, resp.value);
                                numOps ++;
                                break;
                            } else if (resp.getStatus() == -1) {
                                primaryAddress = new InetSocketAddress(resp.getIp(), resp.getPort());
                                System.out.println("Redirect to " + resp.getIp());
                                client = getThriftClient();
                            } else {
                                System.out.println("No leader currently");
                            }
                        }
                    }
                    long diffTime = System.nanoTime() - startTime;
                    writer.write(type?"put :Q":"get "+Long.toString(diffTime)+"\n");
                    totalTime += diffTime/1000;
                }

            } catch (Exception x) {
                //x.printStackTrace();
                client = getThriftClient();
            }
            globalNumOps.addAndGet(numOps);
        }
    }
}
