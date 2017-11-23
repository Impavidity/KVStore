import java.util.ArrayList;
import java.util.List;

/**
 * Created by shipeng on 17-11-21.
 */
public class testRaft {
    static int  NUM_NODE = 3;
    static List<Raft> rafts = new ArrayList<Raft>();
    static Configuration config = new Configuration();
    public static void testRaftEngine() {
        for (int i = 0; i < NUM_NODE; i++) {
            Raft raft = new Raft(config);
            raft.setID(i);
            for (int j = 0; j < NUM_NODE; j++) {
                if (i != j)
                    raft.addPeer(j, new Peer("localhost", 10020+j, j));
            }
            rafts.add(raft);
        }

        for (Raft raft : rafts) {
            raft.start();
        }
        while (true) {

        }
    }
    public static void main(String[] args) {
        testRaftEngine();
    }

}


