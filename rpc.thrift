
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

struct ClientResponse {
  1: i16 status;
  2: string value;
  3: string ip;
  4: i32 port;
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
  ClientResponse Get(1: i32 id,
             2: string key)
  ClientResponse Put(1: i32 id,
           2: string key,
           3: string value)
}