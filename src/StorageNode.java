import org.apache.thrift.*;
import org.apache.thrift.server.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.*;

/**
 * Created by shipeng on 17-11-22.
 */
public class StorageNode {
    static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StorageNode.class);
    public static void main(String[] args) throws TTransportException {
        Configuration config = new Configuration();
        Raft raft = new Raft(config);
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
