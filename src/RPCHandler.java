import java.util.List;

/**
 * Created by shipeng on 17-11-14.
 */
public class RPCHandler {
  public AppendEntriesResponse AppendEntries(int term, int leaderID, int prevLogIndex, int prevLogTerm, List<Log> entries, int leaderCommit) throws org.apache.thrift.TException {
    AppendEntriesResponse response = new AppendEntriesResponse();
    return response;
  }

  public RequestVoteResponse RequestVote(int term, int candidateID, int lastLogIndex, int lastLogTerm) throws org.apache.thrift.TException {
    RequestVoteResponse response = new RequestVoteResponse();
    return response;
  }


}
