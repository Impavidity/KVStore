import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by shipeng on 17-11-14.
 */
public class Raft {

    // Self Node ID
    private int id;

    // Node configuration
    private Configuration config;
    // Election Timeout
    private volatile long electionTimeout;
    // State for each node
    // persistent state on all servers
    private int currentTerm;

    static Semaphore mutex = new Semaphore(1);

    public void setVotedFor(int votedFor) {
        this.votedFor = votedFor;
    }

    private int votedFor;
    private int lastTermCommitted = 0;

    private SecureRandom random = new SecureRandom();



    private int leaderID = -1;

    public Log getLogs() {
        return logs;
    }

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

    public Raft(Configuration config, StateMachine stateMachine, int id) throws IOException {
        this.config = config;
        this.id = id;
        this.logs = new Log(config, stateMachine, this.id);
    }

    private void persist() {
    }

    private void readPersist() {

    }

    synchronized public void setElectionTimeout() {
        this.electionTimeout = System.currentTimeMillis() + this.config.getElectionTimeout() + random.nextInt(500); //TODO: add random timeout?
    }

    private void genThread4PeriodicTask() {
        class periodicTask implements Runnable {
            public void run() {
                while (true) { // TODO: check the role/state ?
                    periodicTask();
//                    try {
//                        Thread.sleep(1); //TODO : Tune Parameters
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }
        Thread t = new Thread(new periodicTask());
        t.start();
//        class periodicTask2 implements Runnable {
//            public void run() {
//                while (true) {
//                    periodicTask2();
//                }
//            }
//        }
//        Thread t2 = new Thread(new periodicTask2());
//        t2.start();
    }

    static AtomicLong updateIndexTime = new AtomicLong(0);
    static AtomicLong updatePeersTime = new AtomicLong(0);
    static AtomicLong periodOps = new AtomicLong(0);

    private void periodicTask2() {
        switch (this.state) {
            case Leader:
                //periodOps.addAndGet(1);
                long starttime = System.nanoTime();
                updateCommitIndex();
                long middletime = System.nanoTime();
                updateIndexTime.addAndGet(middletime-starttime);
                break;
        }
    }

    private void periodicTask() { // TODO: need to be synchronized type?
        //System.out.println("I am alive "+this.id + " " + this.state);
        switch (this.state) {
            case Candidate:
            case Follower:
                if (System.currentTimeMillis() > this.electionTimeout) {
                    this.leaderID = -1;
                    this.votedFor = -1;
                    startElection();
                }
                break;
            case Leader:
                periodOps.addAndGet(1);
                long starttime = System.nanoTime();
                updateCommitIndex();
                long middletime = System.nanoTime();
                updateIndexTime.addAndGet(middletime-starttime);
                updatePeers();
                updatePeersTime.addAndGet(System.nanoTime()-middletime);
                break;
        }

    }

    synchronized private boolean isCommittable(int index) {
        int count = 1;
        int needed = (this.peers.size() + 1) / 2;
        for (Peer p : this.peers.values()) {
            if (p.getMatchIndex() >= index) {
                count ++;
                if (count > needed)
                    return true;
            }
        }
        return count > needed;
    }

    synchronized private void updateCommitIndex() {
        assert(this.state == State.Leader);
        if (isCommittable(firstIndexOfTerm)) {
            // Leader's last index
            int index = this.logs.getLastLogIndex();
            // Loop for peers, to find the smallest index which is commited
            // And then update peers to my current 'index'
            for (Peer peer: this.peers.values()) {
                index = Math.min(index, peer.getMatchIndex());
            } // Does not make sense for me -- Note: Peng
            index = Math.max(index, this.logs.getCommitIndex());
            int length = this.logs.getLastLogIndex();
            while (index <= length && isCommittable(index)) {
                Entry e = this.logs.getEntry(index);
                if (e != null && this.lastTermCommitted != e.term) {
                    StorageNode.logger.info("Committed new term " + e.term);
                    // TODO: some other functions here
                    this.lastTermCommitted = e.term;
                }
                this.logs.setCommitIndex(index);
                index ++;
            }
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
        this.electionTimeout += 5000;
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
        StorageNode.logger.info(this + " Finish the update peers soon after becoming leader");
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
        System.out.println("Updating " + peer);
        RaftRPC.Client client = getClient(peer.getId());
        if (client == null) return;
        synchronized (client) {
            try {
                int prevLogIndex = peer.getNextIndex() - 1;
                int prevLogTerm = this.logs.getTerm(prevLogIndex);
                //StorageNode.logger.info(" I want to get entry from " + peer.getNextIndex() + " to " + this.logs.getLastIndex() + " for " + peer);
                List<Entry> entries = this.logs.getEntries(peer.getNextIndex());
                // Try heartbeat first to make it successful
                AppendEntriesResponse response = client.AppendEntries(this.currentTerm,
                        this.id, prevLogIndex, prevLogTerm, entries, this.logs.getCommitIndex());
                if (this.state == State.Leader) {
                    if (!stepDown(response.term)) {
                        if (response.success) {
                            if (entries != null) {
                                peer.setMatchIndex(entries.get(entries.size()-1).getIndex());
                                peer.setNextIndex(peer.getMatchIndex()+1);
                                //StorageNode.logger.info("Update " + peer + " match index to " + peer.getMatchIndex());
                                //StorageNode.logger.info("Update " + peer + " next Index to " + peer.getNextIndex());
                                assert(peer.getNextIndex() != 0);
                            } else {
                                peer.setNextIndex(Math.max(response.lastLogIndex+1, 0));
                            }
                        } else {
                            if (peer.getNextIndex() > response.lastLogIndex) {
                                peer.setNextIndex(Math.max(response.lastLogIndex + 1, 0));
                            } else if (peer.getNextIndex() > 0) {
                                peer.decreaseNextIndex();
                            }
                        }
                    }
                }
            } catch (TException e) {
                //e.printStackTrace();
                StorageNode.logger.info("Update Peer " + peer.getId() + " failed");
                this.clients.remove(peer.getId());
            }
        }
    }

//    synchronized boolean update(Entry e, int type, String key, String value) {
//        e = new Entry(this.currentTerm, this.logs.getLastLogIndex()+1, type, key, value);
//        return this.logs.append(e);
//    }

    static AtomicInteger globalNumOps = new AtomicInteger(0);
    static AtomicLong mutexTime = new AtomicLong(0);
    static AtomicLong watingStateMachineTime = new AtomicLong(0);
    static AtomicInteger globalExecs = new AtomicInteger(0);

    public ClientResponse executeCommand(int type, int id, String key, String value) {
        globalExecs.addAndGet(1);
        ClientResponse response = new ClientResponse();
        if (state == State.Leader) {
            Entry e;
            boolean r;
            long starttime = System.nanoTime();
            try {
                mutex.acquire();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            e = new Entry(this.currentTerm, this.logs.getLastLogIndex()+1, type, key, value);
            r = this.logs.append(e);
            mutex.release();
            long middletime = System.nanoTime();
            mutexTime.addAndGet(middletime-starttime);

            //StorageNode.logger.info("Append Log " + r);

            if (r) {

                while (e.index > this.logs.getStateMachine().getIndex()) {
                    //StorageNode.logger.info("Waiting for state machine");
                }
                watingStateMachineTime.addAndGet(System.nanoTime()-middletime);
                globalNumOps.addAndGet(1);
                if (e.type == 2) {
                    response.setValue(this.logs.getStateMachine().getValue(e.index));
                    response.setStatus((short)0);
                    if (response.value == null)
                        System.out.println(response.value);
                    //StorageNode.logger.info("Finish Get Operation");
                } else if (e.type == 1) {
                    response.setStatus((short)0);
                    //StorageNode.logger.info("Finish Put Operation");
                }
                // TODO: How to get the result and compose the response
            } else {
                // Client resent this command, if it
                //StorageNode.logger.info("Append Failed");
                // TODO: Feature request: If the Entry is existed (duplicate), then return the duplicate entry
            }
        } else if (leaderID != -1) {
            response.setStatus((short)-1);
            Peer leader = getPeer(leaderID);
            response.setIp(leader.getIp());
            response.setPort(leader.getPort());
        } else {
            response.setStatus((short)-2);
        }
        return response;
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
