import java.util.List;
import java.util.Map;

/**
 * Created by shipeng on 17-11-14.
 */
public class Raft {
  // State for each node
  // persistent state on all servers
  int currentTerm;
  int votedFor;
  List<Entry> log;

  // volatile state on all servers
  int commitIndex;
  int lastApplied;

  // volatile state on leaders
  Map<Integer, Integer> nextIndex;
  Map<Integer, Integer> matchIndex;

  // State of the server
  public enum State {
    Follower, Candidate, Leader
  }
  private State state;

  private void persist() {
  }

  private void readPersist() {

  }

  public void start() {
    this.state = State.Follower;
  }




}
