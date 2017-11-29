import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * Created by shipeng on 17-11-14.
 */
public class Raft {

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



    private int leaderID = -1;
    private Log logs;

    // volatile state on all servers
    private int commitIndex;
    private int lastApplied;
    private int firstIndexOfTerm;

    // volatile state on leaders
    private Map<Integer, Integer> nextIndex;
    private Map<Integer, Integer> matchIndex;

    // State of the server
    public enum State {
        Follower, Candidate, Leader
    }

    private Map<Integer, Peer> peers = new ConcurrentHashMap<>();
    private Map<Integer, RaftRPC.Client> clients = new ConcurrentHashMap<Integer, RaftRPC.Client>();

    private Map<String, String> keyValue = new ConcurrentHashMap<>();

    public String getValue(String key) {
        String result = keyValue.get(key);
        if (result == null)
            result = "";
        return result;
    }

    public boolean putValue(String key, String value) {
        keyValue.put(key, value);
        return true;
    }

    public State getState() {
        return state;
    }

    private State state;

    public Raft(Configuration config, StateMachine stateMachine) throws IOException {
        this.config = config;
        this.logs = new Log(config, stateMachine);
    }

    private void persist() {
    }

    private void readPersist() {

    }

    synchronized public void setElectionTimeout() {
        this.electionTimeout = System.currentTimeMillis() + this.config.getElectionTimeout(); //TODO: add random timeout?
    }

    private void genThread4PeriodicTask() {
        class periodicTask implements Runnable {
            public void run() {
                while (true) { // TODO: check the role/state ?
                    periodicTask();
                    try {
                        Thread.sleep(100);
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
                break;
            case Leader:
                updatePeers();
                break;
        }

    }

    private void startElection() {
        int votesNeeded = (this.peers.size() + 1)/2;
        AtomicInteger votes = new AtomicInteger(1);
        this.state = State.Candidate;
        this.currentTerm += 1;
        this.leaderID = -1;
        this.votedFor = this.id;
        StorageNode.logger.info(this + " is start an election (term "+this.currentTerm + ")");
        // Not create threads for voting currently
        // Sequentially request. Create PRC client, do stuff, close it.
        if (this.peers.size() > 0) {
            for (Map.Entry<Integer, Peer> entry : this.peers.entrySet()) {
                RaftRPC.Client client = getClient(entry.getKey());
                if (client == null) continue;
                synchronized (client) {
                    try {
                        RequestVoteResponse response = client.RequestVote(this.currentTerm,
                                this.id, this.logs.getLastLogIndex(), this.logs.getLastLogTerm());
                        if (!stepDown(response.term)) {
                            if (response.term == this.currentTerm && this.state == State.Candidate) {
                                if (response.voteGranted) {
                                    if (votes.incrementAndGet() > votesNeeded) {
                                        becomeLeader();
                                    }
                                }
                            }
                        }
                    } catch (TException e) {
                        StorageNode.logger.info("Request vote to peer " + entry.getKey() + " failed");
                        this.clients.remove(entry.getKey());
                        //e.printStackTrace();
                    }
                }
            }
        } else {
            becomeLeader();
        }
        setElectionTimeout();
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


    public Peer getPeer(int id) {
        return this.peers.get(id);
    }

    public RaftRPC.Client getClient(int id) {
        RaftRPC.Client client = this.clients.get(id);
        if (client == null) {
            try {
                Peer peer = this.peers.get(id);
                TSocket sock = new TSocket(peer.getIp(), peer.getPort());
                TTransport transport = new TFramedTransport(sock);
                transport.open();
                TProtocol protocol = new TBinaryProtocol(transport);
                this.clients.put(id, new RaftRPC.Client(protocol));
                return this.clients.get(id);
            } catch (Exception e) {
                //e.printStackTrace();
                return null;
            }
        } else {
            return client;
        }
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public int getVotedFor() {
        return votedFor;
    }

    public int getLastLogIndex() {
        return logs.getLastLogIndex();
    }

    public int getLastLogTerm() {
        return logs.getLastLogTerm();
    }

    public void setState(State state) {
        this.state = state;
    }

    private void clearAllPendingRequests() {

    }

    synchronized public boolean stepDown(int term) {
        if (term > this.currentTerm) {
            this.currentTerm = term;
            votedFor = -1;
            if (this.state == State.Candidate || this.state == State.Leader) {
                this.state = State.Follower;
                StorageNode.logger.info(this + " is stepping down (term" + term + ")");
                clearAllPendingRequests();
            }
            setElectionTimeout();
            return true;
        }
        return false;
    }

    synchronized private void becomeLeader() {
        StorageNode.logger.info(this + " become leader");
        this.leaderID = this.id;
        this.state = State.Leader;
        this.firstIndexOfTerm = this.logs.getLastLogIndex() + 1;
        for (Peer peer : peers.values()) {
            // Set some info here
        }
        updatePeers();
    }

    synchronized private void updatePeers() {
        if (this.state == State.Leader) {
            for (Peer peer: this.peers.values()) {
                updatePeer(peer);
            }
        } else {
            StorageNode.logger.error(this + " is not leader but update peers");
        }
    }

    synchronized private void updatePeer(Peer peer) {
        RaftRPC.Client client = getClient(peer.getId());
        if (client == null) return;
        synchronized (client) {
            try {
                // Try heartbeat first to make it successful
                AppendEntriesResponse response = client.AppendEntries(this.currentTerm,
                        this.id, -1, -1, null, -1);
            } catch (TException e) {
                //e.printStackTrace();
                StorageNode.logger.info("Update Peer " + peer.getId() + " failed");
                this.clients.remove(peer.getId());
            }
        }
    }

    public void setLeaderID(int leaderID) {
        this.leaderID = leaderID;
    }

    public int getLeaderID() {
        return leaderID;
    }

    public int getId() {
        return id;
    }
}
