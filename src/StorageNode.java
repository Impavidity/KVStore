import org.apache.thrift.*;
import org.apache.thrift.server.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * Created by shipeng on 17-11-22.
 */
public class StorageNode {

    static Logger logger;

    public static void main(String[] args) throws TTransportException {
        BasicConfigurator.configure();
        logger = Logger.getLogger(StorageNode.class);
        Configuration config = new Configuration();
        Raft raft = new Raft(config);
        raft.setID(Integer.parseInt(args[0]));
        logger.info("My ID is " + args[1]);
        for (int i=2; i<args.length; i++) {
            String paras[] = args[i].split(":");
            int id = Integer.parseInt(paras[0]);
            String ip = paras[1];
            int port = Integer.parseInt(paras[2]);
            Peer peer = new Peer(ip, port, id);
            raft.addPeer(id, peer);
        }
        raft.start();
        RPCHandler handler = new RPCHandler(raft);
        RaftRPC.Processor<RaftRPC.Iface> processor = new RaftRPC.Processor<>(handler);
        TServerSocket socket = new TServerSocket(Integer.parseInt(args[1]));
        TThreadPoolServer.Args sargs = new TThreadPoolServer.Args(socket);
        sargs.protocolFactory(new TBinaryProtocol.Factory());
        sargs.transportFactory(new TFramedTransport.Factory());
        sargs.processorFactory(new TProcessorFactory(processor));
        sargs.maxWorkerThreads(64);
        TServer server = new TThreadPoolServer(sargs);
        logger.info("Launching server");
        server.serve();
    }
}
