import java.util.List;

/**
 * Created by shipeng on 17-11-14.
 */
public class RPCHandler implements RaftRPC.Iface {
    private Raft raft;
    RPCHandler(Raft raft) {
        this.raft = raft;
    }
    public AppendEntriesResponse AppendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<Entry> entries, int leaderCommit) throws org.apache.thrift.TException {
        AppendEntriesResponse response = new AppendEntriesResponse();
        return response;
    }

    public RequestVoteResponse RequestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws org.apache.thrift.TException {
        // TODO: Check candidate ID is valid or not
        RequestVoteResponse response = new RequestVoteResponse();
        return response;
    }


}
