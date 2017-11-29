import java.io.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    private static final Logger logger = LoggerFactory.getLogger(Log.class);

    private final List<Entry> entries = new ArrayList<>();

    private final Configuration config;

    private final StateMachine stateMachine;

    private DataOutputStream out;

    private int firstIndex = 0;
    private int firstTerm = 0;
    private int lastIndex = -1;
    private int lastTerm = -1;
    private int commitIndex = 0;

    public Log(Configuration config, StateMachine stateMachine) throws IOException {
        this.config = config;
        this.stateMachine = stateMachine;

        this.config.getLogDirector().mkdirs();

        replayLogs();

        updateStateMachine();

        class periodicTask implements Runnable {
            public void run() {
                periodicTask();
            }
        }
        Thread t = new Thread(new periodicTask());
        t.start();
    }

    synchronized public StateMachine getStateMachine() {
        return stateMachine;
    }

    synchronized public int getFirstIndex() {
        return firstIndex;
    }

    synchronized public int getFirstTerm() {
        return firstTerm;
    }

    synchronized public int getLastLogIndex() {
        return lastIndex;
    }

    synchronized public int getLastLogTerm() {
        return lastTerm;
    }

    synchronized public int getCommitIndex() {
        return commitIndex;
    }

    public File getLogDirectory() {
        return config.getLogDirector();
    }

    public List<Entry> getEntries() {
        return entries;
    }

    synchronized public boolean append(Entry entry) {
        if (entry.index <= lastIndex) {
            if (getTerm(entry.index) != entry.term) {
                logger.warn("Log is conflicted at {} : {} ", entry, getTerm(entry.index));
                wipeConflictedEntries(entry.index);
            } else {
                return true;
            }
        }

        if (entry.index == lastIndex + 1 && entry.term >= lastTerm) {
            entries.add(entry);

            if (firstIndex == 0) {
                assert (entries.size() == 1);

                firstIndex = entry.index;
                firstTerm = entry.term;

                logger.info("Setting First Index = {} ({})", firstIndex, entry.index);
            }
            lastIndex = entry.index;
            lastTerm = entry.term;

            return true;
        }

        return false;
    }

    public int getTerm(int index) {
        if (index == 0) {
            return 0;
        }

        if (index == stateMachine.getIndex()) {
            return stateMachine.getTerm();
        }

        final Entry entry = getEntry(index);
        if (entry == null) {
            logger.error("Could not find entry in log for {}", index);
        }
        return entry.term;
    }

    synchronized public void wipeConflictedEntries(long index) {
        if (index <= commitIndex) {
            throw new RuntimeException("Can't restore conflicted index already written to disk: " + index);
        }

        while (lastIndex >= index) {
            entries.remove((int) (lastIndex-- - firstIndex));
        }
        if (lastIndex > 0) {
            lastTerm = getTerm(lastIndex);
        } else {
            lastTerm = 0;
        }
    }

    private void periodicTask() {
        while (true) {
            try {
                updateStateMachine();
                compact();
                if (out != null) {
                    out.flush();
                }
                synchronized (this) {
                    wait(100);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private synchronized void replayLogs() throws IOException {
        Entry entry;
        do {
            entry = getEntryFromDisk(stateMachine.getIndex() + 1);
            if (entry != null) {
                stateMachine.apply(entry);
            }
        } while (entry != null);

        File file = new File(getLogDirectory(), "requests.log");
        final List<Entry> list = loadLogFile(file);
        if (list != null && list.size() > 0) {
            assert (entries.size() == 0);
            entries.addAll(list);
            firstIndex = entries.get(0).index;
            firstTerm = entries.get(0).term;
            lastIndex = entries.get(entries.size() - 1).index;
            lastTerm = entries.get(entries.size() - 1).term;
            out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            for (Entry e : list) {
                out.write(e.term);
                out.write(e.index);
                out.write(e.type);
                out.writeUTF(e.key);
                out.writeUTF(e.value);
            }
            out.flush();
            commitIndex = lastIndex;
            logger.info("Log First Index = {}, Last Index = {}", firstIndex, lastIndex);
        }
    }

    synchronized public Entry getEntry(int index) {
        if (index > 0 && index <= lastIndex) {
            if (index >= firstIndex && entries.size() > 0) {
                assert (index - firstIndex < Integer.MAX_VALUE);
                assert (firstIndex == entries.get(0).index);

                final Entry entry = entries.get(index - firstIndex);
                return entry;
            } else {
                try {
                    return getEntryFromDisk(index);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private Entry getEntryFromDisk(int index) throws IOException {
        File file = new File(getLogDirectory(), "requests.log");
        if (file.exists()) {
            List<Entry> list = loadLogFile(file);
            if (list != null && list.size() > 0) {
                if (index >= 0 && index < list.size()) {
                    return list.get(index);
                }
            }
        } else {
            logger.info("Can not find file {}", file);
        }
        return null;
    }

    public List<Entry> loadLogFile(File file) throws IOException {
        List<Entry> list =  new ArrayList<>();
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            while (true) {
                int term = in.readInt();
                int index = in.readInt();
                int type = in.readInt();
                String key = in.readUTF();
                String value = in.readUTF();
                final Entry entry = new Entry(term, index, type, key, value);
                list.add(entry);
            }
        } catch (EOFException e) {
            logger.debug("Read {} from {}", list.size(), file);
        }
        return list;
    }

    public synchronized void updateStateMachine() {
        try {
            synchronized (stateMachine) {
                while (commitIndex > stateMachine.getIndex()) {
                    final Entry entry = getEntry(stateMachine.getIndex() + 1);
                    assert (entry != null);
                    assert (entry.index == stateMachine.getIndex() + 1);
                    stateMachine.apply(entry);

                    if (out == null) {
                        File file = new File(getLogDirectory(), "requests.log");
                        out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
                    }

                    out.write(entry.term);
                    out.write(entry.index);
                    out.write(entry.type);
                    out.writeUTF(entry.key);
                    out.writeUTF(entry.value);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private synchronized void compact() {
        if (entries.size() > config.getThreshold()) {
            if (firstIndex > commitIndex || firstIndex > stateMachine.getIndex()) {
                return;
            }

            List<Entry> entriesToKeep = new ArrayList<>();
            for (Entry entry : entries) {
                if (entry.index > commitIndex || entry.index > stateMachine.getIndex()) {
                    entriesToKeep.add(entry);
                }
            }
            entries.clear();
            entries.addAll(entriesToKeep);
            Entry firstEntry = entries.get(0);
            firstIndex = firstEntry.index;
            firstTerm = firstEntry.term;
            logger.info("Compacted log new size = {}, firstIndex = {}", entries.size(), firstIndex);
        }
    }
}
