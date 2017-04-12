package edu.utdallas.project3.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

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
	private static final Logger logger = LogManager.getLogger(MutexServer.class.getName());
	private static final Marker CRITICAL_SECTION_MARKER = MarkerManager.getMarker("CRITICAL_SECTION");
	
    private static final String MUTEX_ALGORITHM = "mutex.algorithm";
    private static final String MUTEX_LAMPORT = "mutex.lamport";
    private static final String MUTEX_RICART_AND_AGRAWALA = "mutex.ricart.and.agrawala";
    private static final String DEFAULT_MUTEX_ALGORITHM = MUTEX_LAMPORT;
    
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
        
        /* Use thread pools to manage process behaviors */
        ExecutorService pool = Executors.newFixedThreadPool(50);
        
        Linker linker = null;
        try {
            /* Setup long connections with all neighbors */
            linker = new Linker(myId, config.getNeighbors());
            linker.buildChannels(port);
            
            String mutexAlgorithm = System.getProperty(MUTEX_ALGORITHM, DEFAULT_MUTEX_ALGORITHM);
            
            Lock lock = null;
            Process proc = new Process(linker, config);
            if (mutexAlgorithm.equals(MUTEX_LAMPORT)) {
                lock = new LamportMutex(proc);
            } else if (mutexAlgorithm.equals(MUTEX_RICART_AND_AGRAWALA)) {
                lock = new RAMutex(proc);
            } else {
                throw new UndefinedPropertyException("Mutex algorithm not set");
            }
                
            for(Node node : linker.getNeighbors()){
                Runnable task = new ListenerThread(myId, node.getNodeId(), lock);
                pool.execute(task);
            }

            int meanInterRequestDelay = config.getMeanInterRequestDelay();
            int meanCSExecution = config.getMeanCSExecution();
            int numberOfRequest = config.getNumberOfRequest();
            ExponentialDistribution edIRD = new ExponentialDistribution(meanInterRequestDelay);
            ExponentialDistribution edCSE = new ExponentialDistribution(meanCSExecution);
            final Lock effectiveLock = lock;
            Runnable criticalSectionTask = () -> {
            	try {
            		boolean isFirstTime = true;
	            	for (int i = 0; i < numberOfRequest; i++){
	            		if (!isFirstTime){
	            		    Thread.sleep((long) edCSE.sample());
	            		}
	            		effectiveLock.csEnter();
	            		logger.info(CRITICAL_SECTION_MARKER, "[Node {}] Enter critical section", myId);
						Thread.sleep((long) edIRD.sample());
						logger.info(CRITICAL_SECTION_MARKER, "[Node {}] Leave critical section", myId);
						effectiveLock.csLeave();
						isFirstTime = false;
	            	}
	            	logger.info("[Node {}] Critical section done..", myId);
	            	effectiveLock.broadcastTerminationMessage();
            	} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
            };
            pool.execute(criticalSectionTask);
            lock.waitForDone();
            
        } catch (UndefinedPropertyException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
            if (linker != null){
                linker.close();
            }
        }
        
    }
    


}
