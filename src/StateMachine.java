import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StateMachine {
    private int index = -1;
    private int term = 0;

    private Map<String, String> map;
    private Map<Integer, String> indexValue;

    public StateMachine() {
        map = new ConcurrentHashMap<>();
        indexValue = new ConcurrentHashMap<>();
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
        } else if (entry.type == 2 && entry.key != null) {
            indexValue.put(entry.index, map.getOrDefault(entry.key, ""));
        }

        this.index = entry.index;
        this.term = entry.term;
    }

    public String getValue(int index) {
        String value = indexValue.get(index);
        //if (value == null) return "";
        //else return value;
        return value;
    }
}
