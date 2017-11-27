/**
 * Created by shipeng on 17-11-22.
 */
public class Log {

    private int lastIndex = -1;
    private int lastTerm = -1;
    synchronized public int getLastLogIndex() {
        return lastIndex;
    }

    synchronized public int getLastLogTerm() {
        return lastTerm;
    }


}
