import java.io.File;

public class Configuration {

    private int ELECTION_TIMEOUT = 2000;
    private int THRESHOLD = 1000;

    private File logDirectory = new File("raft");

    public int getElectionTimeout() {
        return ELECTION_TIMEOUT;
    }

    public File getLogDirectory() {
        return logDirectory;
    }

    public int getThreshold() {
        return THRESHOLD;
    }


}
