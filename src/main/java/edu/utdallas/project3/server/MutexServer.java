package edu.utdallas.project3.server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.utdallas.project3.protocol.LamportMutex;
import edu.utdallas.project3.protocol.Lock;
import edu.utdallas.project3.protocol.RAMutex;
import edu.utdallas.project3.socket.Linker;
import edu.utdallas.project3.tools.MutexConfig;



/**
 * 
 * @author Zeqing Li, The University of Texas at Dallas
 * @author Ming Sun, The University of Texas at Dallas
 * @author Jingyi Liu, The University of Texas at Dallas
 */
public class MutexServer {    
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
        if (args.length != 3) {
            System.err.println("Usage: java Server <port> <node id> <config file>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        int myId = Integer.parseInt(args[1]);
        String configurationFilePath = args[2];
        String strategyName = args[3];
        
        
        MutexConfig config = MutexConfig.loadFromConfigurationFile(configurationFilePath, myId);
        
        final MutexServer server = new MutexServer(config);
        
        Linker linker = new Linker(myId, config.getNeighbors());
        
        
        /* Use thread pools to manage process behaviors */
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        
        try {
            linker.buildChannels(port);
            try {
                Lock lock = null;
                if (strategyName.equals("Lamport"))
                    lock = new LamportMutex(linker, config);
                if (strategyName.equals("RicartAgrawala"))
                    lock = new RAMutex(linker, config);
                
                for(Node node : linker.getNeighbors()){
                    Runnable task = new ListenerThread(myId, node.getNodeId(), lock);
                    executorService.execute(task);
                }
                while (true) {
                    System.out.println(myId + " is not in CS");
                    Thread.sleep(2000);
                    lock.csEnter();
                    Thread.sleep(2000);
                    System.out.println(myId + " is in CS *****");
                    lock.csLeave();
                }
            }
            catch (InterruptedException e) {
                if (linker != null) linker.close();
            }
            catch (Exception e) {
                System.out.println(e);
                e.printStackTrace();
            }

            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            linker.close();
        }
        
    }
    


}
