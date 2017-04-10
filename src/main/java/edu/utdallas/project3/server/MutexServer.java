package edu.utdallas.project3.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.utdallas.project3.protocol.LamportMutex;
import edu.utdallas.project3.protocol.Lock;
import edu.utdallas.project3.protocol.RAMutex;
import edu.utdallas.project3.socket.Linker;
import edu.utdallas.project3.tools.MutexConfig;
import edu.utdallas.project3.tools.UndefinedPropertyException;



/**
 * 
 * @author Zeqing Li, The University of Texas at Dallas
 * @author Ming Sun, The University of Texas at Dallas
 * @author Jingyi Liu, The University of Texas at Dallas
 */
public class MutexServer {    
    private static final String MUTEX_ALGORITHM = "mutex.algorithm";
    private static final String MUTEX_LAMPORT = "mutex.lamport";
    private static final String MUTEX_RICART_AND_AGRAWALA = "mutex.ricart.and.agrawala";
    private static final String DEFAULT_MUTEX_ALGORITHM = MUTEX_LAMPORT;
    
    MutexConfig config = null;
    
    public MutexServer(MutexConfig config){
        this.config = config;
    }
    
    /**
     * 
     * @param args <port> <node id> <config-file>
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2 || args.length > 3) {
            System.err.println("Usage: java MutexServer <port> <node id> [config file]");
            
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        int myId = Integer.parseInt(args[1]);
        
        MutexConfig config;
        if (args.length == 3 ) {
            String configurationFilePath = args[2];
            config = MutexConfig.loadFromConfigurationFile(configurationFilePath, myId);
        } else {
            InputStream in = MutexServer.class.getClassLoader().getResourceAsStream("config.txt");
            config = MutexConfig.loadFromConfigurationFile(in, "config.txt", myId);
        }
        
        /* Configure a Mutex server */
        final MutexServer server = new MutexServer(config);
        
        
        
        /* Use thread pools to manage process behaviors */
        ExecutorService pool = Executors.newFixedThreadPool(50);
        
        Linker linker = null;
        try {
            /* Setup long connections with all neighbors */
            linker = new Linker(myId, config.getNeighbors());
            linker.buildChannels(port);
            
            String mutexAlgorithm = System.getProperty(MUTEX_ALGORITHM, DEFAULT_MUTEX_ALGORITHM);

            Lock lock = null;
            if (mutexAlgorithm.equals(MUTEX_LAMPORT)) {
                lock = new LamportMutex(linker, config);
            } else if (mutexAlgorithm.equals(MUTEX_RICART_AND_AGRAWALA)) {
                lock = new RAMutex(linker, config);
            } else {
                throw new UndefinedPropertyException("Mutex algorithm not set");
            }
                
            for(Node node : linker.getNeighbors()){
                Runnable task = new ListenerThread(myId, node.getNodeId(), lock);
                pool.execute(task);
            }
            
//            ((LamportMutex)lock).sendToNeighbors(MessageType.DEFAULT, "");
//            ((LamportMutex)lock).broadcastReleaseMessage(5);
            
            System.out.print(myId + " is not in CS\n");
            Thread.sleep(2000);
            lock.csEnter();
            Thread.sleep(2000);
            System.out.print(myId + " is in CS *****\n");
            lock.csLeave();

            Thread.sleep(10000);
            
        } catch (UndefinedPropertyException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
            if (linker != null){
                linker.close();
            }
        }
        
    }
    


}
