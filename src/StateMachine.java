import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StateMachine {
    private int index = 0;
    private int term = 0;

    private Map<String, String> map;

    public StateMachine() {
        map = new ConcurrentHashMap<>();
    }

    public int getIndex() {
        return index;
    }

    public int getTerm() {
        return term;
    }

    public void apply(Entry entry) {
        assert (this.index + 1 == entry.index) : (this.index + 1) + "!=" + entry.index;
        assert (this.term <= entry.term);
        if (entry.type == 1 && entry.key != null && entry.value != null) {
            map.put(entry.key, entry.value);
        }
        this.index = entry.index;
        this.term = entry.term;
    }
}
