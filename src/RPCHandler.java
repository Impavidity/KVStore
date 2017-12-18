import java.util.List;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * Created by shipeng on 17-11-14.
 */
public class RPCHandler implements RaftRPC.Iface {
//    static Logger logger;
    private Raft raft;
    RPCHandler(Raft raft) {
        this.raft = raft;
//        BasicConfigurator.configure();
//        logger = Logger.getLogger(RPCHandler.class);
    }
    /*
    * AppendEntries RPC
    * 1. Reply false if term < currentTerm
    * 2. Reply false if log doesn't contain an entry at prevLogIndex whose term matches prevLogTerm
    * 3. If an existing entry conflicts with a new one (same index but different terms), delete the existing entry
    *    and all that follow it.
    * 4. Append any new entries not already in the log
    * 5. If leaderCommit > commitIndex, set commitIndex = min(leaderCommit, index of last new entry)
    * */
    public AppendEntriesResponse AppendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<Entry> entries, int leaderCommit) throws org.apache.thrift.TException {
        //StorageNode.logger.info(this + " receive entries from " + leaderID);
        if (term >= raft.getCurrentTerm()) {
            if (term > raft.getCurrentTerm()) {
                StorageNode.logger.info(term + " > " + raft.getCurrentTerm() + " Step Down");
                raft.stepDown(term);
            }
            if (raft.getLeaderID() != leaderID) {
                raft.setLeaderID(leaderID);
                raft.stepDown(term);
                // Prevent it pass in stepDown, and change it into Candidate due to timeout
                raft.setState(Raft.State.Follower);
            }
            raft.setElectionTimeout();

            if (raft.getLogs().isConsistentWith(prevLogIndex, prevLogTerm)) {
                if (entries != null) {
                    //StorageNode.logger.info(this + " receive entries from " + leaderID + " Entry Length : " + entries.size());
                    for (Entry e: entries) {
                        if (! raft.getLogs().append(e)) {
                            // Append Failed
                            AppendEntriesResponse response =
                                    new AppendEntriesResponse(raft.getCurrentTerm(), false, raft.getLogs().getLastLogIndex());
                            return response;
                        } else {
                            raft.getLogs().append(e);
                        }
                    }
                }

                raft.getLogs().setCommitIndex(Math.min(leaderCommit, raft.getLogs().getLastLogIndex()));
                if (entries != null) {
                    //StorageNode.logger.info(raft + " is fine with append entries from " + leaderID);
                    //StorageNode.logger.info(raft + " set last Log Index as " + raft.getLogs().getLastLogIndex());
                }
//                else
//                    StorageNode.logger.info(raft + " is fine with heartbeat from " + leaderID);
                AppendEntriesResponse response = new AppendEntriesResponse(raft.getCurrentTerm(), true, raft.getLogs().getLastLogIndex());
                return response;
            } else {
                StorageNode.logger.warn(raft + " is inconsistent with " + leaderID);
                StorageNode.logger.warn("Leader PrevLogTerm = " + prevLogTerm + " PrevLogIndex = " + prevLogIndex);
                StorageNode.logger.warn("Follower firstTerm = " + raft.getLogs().getFirstTerm() + " firstIndex = " + raft.getLogs().getFirstIndex());
                StorageNode.logger.warn("Follower lastTerm = " + raft.getLogs().getLastTerm() + " lastIndex = " + raft.getLogs().getLastIndex());
                if (prevLogIndex > raft.getLogs().getCommitIndex()) {
                    raft.getLogs().wipeConflictedEntries(prevLogIndex);
                } else {
                    // TODO: stop machine
                }
                // Rewrite
            }
        }
        AppendEntriesResponse response = new AppendEntriesResponse(raft.getCurrentTerm(), false, raft.getLogs().getLastLogIndex());
        return response;
    }

    /**
     * Receiver implementation:
     1.  Reply false if term < currentTerm (§5.1)
     2.  If votedFor is null or candidateId, and candidate’s log is at
         least as up-to-date as receiver’s log, grant vote (§5.2, §5.4)
     */
    public RequestVoteResponse RequestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws org.apache.thrift.TException {
        // TODO: Check candidate ID is valid or not
        if (term > raft.getCurrentTerm()) {
            raft.stepDown(term);
        }
        if (term >= raft.getCurrentTerm() && (raft.getVotedFor()==-1 || raft.getVotedFor()==candidateID)
                && lastLogIndex >= raft.getLastLogIndex() && lastLogTerm >= raft.getLastLogTerm()) {
            raft.setState(Raft.State.Follower);
            StorageNode.logger.info(this + " voted for " + candidateID);
            raft.setVotedFor(candidateID);
            raft.setElectionTimeout();
            return new RequestVoteResponse(raft.getCurrentTerm(), true);
        } else {
            StorageNode.logger.info(this + " reject " + candidateID + " because term " + term +
                            " my term " + raft.getCurrentTerm() + " vote for " +
                            raft.getVotedFor() + " last log " + lastLogIndex + " " + raft.getLastLogIndex() +
                            " last term " + lastLogTerm + " " + raft.getLastLogTerm());
            return new RequestVoteResponse(raft.getCurrentTerm(), false);
        }
    }
    // 0: success
    // -1 : Not leader, redirect to other server
    // -2 : I do not know leader currently
    public ClientResponse Get(int id, String key) throws org.apache.thrift.TException {
        ClientResponse response = new ClientResponse();
        if (raft.getState() == Raft.State.Leader) {
            // Type 1 is Get
            response = raft.executeCommand(2, id, key, "");
            //response.setValue(raft.getValue(key));
            //response.setStatus((short)0);
        } else if (raft.getLeaderID() != -1) {
            response.setStatus((short)-1);
            Peer leader = raft.getPeer(raft.getLeaderID());
            response.setIp(leader.getIp());
            response.setPort(leader.getPort());
        } else {
            response.setStatus((short)-2);
        }
        return response;
    }

    public ClientResponse Put(int id, String key, String value) throws org.apache.thrift.TException {
        ClientResponse response = new ClientResponse();
        if (raft.getState() == Raft.State.Leader) {
//            if (raft.putValue(key, value))
//                response.setStatus((short)0);
            // Type 2: Put
            response = raft.executeCommand(1, id, key, value);
        } else if (raft.getLeaderID() != -1) {
            response.setStatus((short)-1);
            Peer leader = raft.getPeer(raft.getLeaderID());
            response.setIp(leader.getIp());
            response.setPort(leader.getPort());
        } else {
            response.setStatus((short)-2);
        }
        return response;
    }
}
