
struct AppendEntriesResponse {
  1: i32 term;
  2: bool success;
}

struct RequestVoteResponse {
  1: i32 term;
  2: bool voteGranted;
}

struct Entry {
  1: i32 key;
}

service RaftRPC {
  AppendEntriesResponse AppendEntries(1: i32 term,
                                      2: i32 leaderID,
                                      3: i32 prevLogIndex,
                                      4: i32 prevLogTerm,
                                      5: list<Entry> entries,
                                      6: i32 leaderCommit)
  RequestVoteResponse RequestVote(1: i32 term,
                                  2: i32 candidateID,
                                  3: i32 lastLogIndex,
                                  4: i32 lastLogTerm)
}