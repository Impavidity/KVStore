/**
 * Created by shipeng on 17-11-22.
 */
public class StateMachine {
    public enum SnapshotMode {
        Blocking
    }

    public SnapshotMode getSnapshotMode() {
        return SnapshotMode.Blocking;
    }


}
