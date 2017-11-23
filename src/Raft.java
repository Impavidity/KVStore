import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by shipeng on 17-11-14.
 */
public class Raft {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Raft.class);
    // Self Node ID
    private int id;

    // Node configuration
    private Configuration config;
    // Election Timeout
    private long electionTimeout;
    // State for each node
    // persistent state on all servers
    private int currentTerm;
    private int votedFor;
    private Log logs;

    // volatile state on all servers
    private int commitIndex;
    private int lastApplied;

    // volatile state on leaders
    private Map<Integer, Integer> nextIndex;
    private Map<Integer, Integer> matchIndex;

    // State of the server
    public enum State {
        Follower, Candidate, Leader
    }

    private Map<Integer, Peer> peers = new HashMap<Integer, Peer>();
    private Map<Integer, RaftRPC.Client> clients = new ConcurrentHashMap<Integer, RaftRPC.Client>();



    private State state;

    public Raft(Configuration config) {
        this.config = config;
    }

    private void persist() {
    }

    private void readPersist() {

    }

    synchronized private void setElectionTimeout() {
        this.electionTimeout = System.currentTimeMillis() + this.config.getElectionTimeout(); //TODO: add random timeout?
    }

    private void genThread4PeriodicTask() {
        class periodicTask implements Runnable {
            public void run() {
                while (true) { // TODO: check the role/state ?
                    periodicTask();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Thread t = new Thread(new periodicTask());
        t.start();
    }

    private void periodicTask() { // TODO: need to be synchronized type?
        System.out.println("I am alive "+this.id + " " + this.state);
        switch (this.state) {
            case Follower:
                if (System.currentTimeMillis() > this.electionTimeout) {
                    startElection();
                }
        }

    }

    private void startElection() {
        int votesNeeded = (this.peers.size() + 1)/2;
        AtomicInteger votes = new AtomicInteger(1);
        this.state = State.Candidate;
        this.currentTerm += 1;
        logger.info("{} is start an election (term {})", this, this.currentTerm);
        // Not create threads for voting currently
        // Sequentially request. Create PRC client, do stuff, close it.
        if (this.peers.size() > 0) {
            for (Map.Entry<Integer, Peer> entry: this.peers.entrySet()) {
                RaftRPC.Client client = getClient(entry.getKey());
                synchronized (client) {
                    try {
                        RequestVoteResponse response = client.RequestVote(this.currentTerm,
                                this.id, this.logs.getLastLogIndex(), this.logs.getLastLogTerm());
                        if (response.term == this.currentTerm && this.state == State.Candidate) {
                            if (response.voteGranted) {
                                if (votes.incrementAndGet() > votesNeeded) {
                                    becomeLeader();
                                }
                            }
                        }
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            becomeLeader();
        }
        setElectionTimeout();
    }

    synchronized void becomeLeader() {

    }

    public void start() {
        this.state = State.Follower;
        setElectionTimeout();
        // Increase election timeout during start, ensure all nodes are ready
        this.electionTimeout += 10000;
        genThread4PeriodicTask();

    }

    public void setID(int id) {
        this.id = id;
    }

    public void addPeer(int id, Peer peer) {
        this.peers.put(id, peer);
    }

    public RaftRPC.Client getClient(int id) {
        RaftRPC.Client client = this.clients.get(id);
        if (client == null) {
            while (true) {
                try {
                    Peer peer = this.peers.get(id);
                    TSocket sock = new TSocket(peer.getIp(), peer.getPort());
                    TTransport transport = new TFramedTransport(sock);
                    transport.open();
                    TProtocol protocol = new TBinaryProtocol(transport);
                    this.clients.put(id, new RaftRPC.Client(protocol));
                    return this.clients.get(id);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } else {
            return client;
        }
    }




}