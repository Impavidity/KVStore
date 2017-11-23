import java.util.List;

/**
 * Created by shipeng on 17-11-14.
 */
public class RPCHandler implements RaftRPC.Iface {
    static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RPCHandler.class);

    private Raft raft;
    RPCHandler(Raft raft) {
        this.raft = raft;
    }
    public AppendEntriesResponse AppendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<Entry> entries, int leaderCommit) throws org.apache.thrift.TException {
        AppendEntriesResponse response = new AppendEntriesResponse();
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
            logger.info("{} voted for {}",this, candidateID);
            raft.setElectionTimeout();
            return new RequestVoteResponse(raft.getCurrentTerm(), true);
        } else {
            logger.info("{} reject {} because term {} my term {} votefor {} last log {} {} lastcterm {} {}",
                    this, candidateID, term, raft.getCurrentTerm(), raft.getVotedFor(),
                    lastLogIndex, raft.getLastLogIndex(), lastLogTerm, raft.getLastLogTerm());
            return new RequestVoteResponse(raft.getCurrentTerm(), false);
        }
    }


}
