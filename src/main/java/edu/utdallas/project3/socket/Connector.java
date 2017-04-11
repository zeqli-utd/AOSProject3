package edu.utdallas.project3.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.utdallas.project3.server.Message;
import edu.utdallas.project3.server.MessageType;
import edu.utdallas.project3.server.Node;


/**
 * A collection of accept sockets for maintaining channels
 * @author zeqing
 *
 */
public class Connector {
    
    private static final Logger logger = LogManager.getLogger(Connector.class.getName());
    
    private static final String CONNECTION_MODE = "connection.mode";
    private static final String LOOPBACK = "loopback";
    private static final String REMOTE = "remote";
    
    
    private ServerSocket listener;
    private Map<Integer, Socket> session;
    
    private static volatile Connector instance = null;
    
    private Connector(){
        session = new HashMap<>();
    }
    
    public static Connector getInstance() {
        // Double checking lock for thread safe.
        if(instance == null){
            synchronized (Connector.class) {
                if(instance == null){
                    instance = new Connector();
                }
            }
        }
        return instance;
    }
    

    /**
     * Caveat: processes must be sorted
     * 
     * @param listenPort
     * @param myId
     * @param in
     * @param out
     * @param processes Sorted list of nodes containing nodeId, host address, and port.
     * @throws IOException 
     * @throws ClassNotFoundException 
     * @throws InterruptedException 
     * @throws Exception
     */
    public void connect(
            int listenPort, 
            int myId, 
            Map<Integer, ObjectInputStream> inMap, 
            Map<Integer, ObjectOutputStream> outMap, 
            List<Node> processes) throws IOException, ClassNotFoundException, InterruptedException {

        logger.trace("[Node {}] [Connect: Phase 0] Setup Server at port {}", myId, listenPort);
        
        // Select connection mode: Remote or Loopback 
        String connectionMode = System.getProperty(CONNECTION_MODE, REMOTE);
        if (connectionMode.equalsIgnoreCase(REMOTE)){
            listener = new ServerSocket(listenPort);
        } else {
            listener = new ServerSocket(listenPort, 0, InetAddress.getLoopbackAddress());
        }
        
        // Accept connections from all the smaller processes 
        int numRecved = 0;
        logger.trace("[Node {}] [Connect: Phase 0] Targets: {}", myId, processes.toString());
        
        while(numRecved < processes.size() && processes.get(numRecved).getNodeId() < myId){
            logger.trace("[Node {}] [Connect:Phase 1] Status: numRecved {}, nodeId {}",  
            		myId, numRecved, processes.get(numRecved).getNodeId());
            Socket socket = listener.accept();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            
            // Read the first message from new request.
            Message msg = (Message)ois.readObject();
            logger.trace("[Node {}] [Connect:Phase 1] Receive {}", myId, msg.toString());
            
            int sourceId = msg.getSourceId();
            if(msg.getMessageType().equals(MessageType.HANDSHAKE)){
                // Register socket.
                session.put(sourceId, socket);
                inMap.put(sourceId, ois);
                outMap.put(sourceId, oos);
                msg = new Message(myId, sourceId, MessageType.HANDSHAKE, "Response");
                oos.writeObject(msg);
                numRecved++;
            }
        }
        logger.trace("[Node {}] [Connect:Phase 1] Complete.", myId);
        /* Contact all the bigger process*/
        while(numRecved < processes.size()){
            Node process = processes.get(numRecved);
            int remoteId = process.getNodeId(), port = process.getPort();
            String host = process.getHostName();
            
            boolean connected = false;
            while(!connected){
                Socket socket = null;
                try{ 
                    logger.trace("[Node {}] [Connect:Phase 2] Connect to {}:{}", myId, host, port);
                    if (connectionMode.equalsIgnoreCase(REMOTE)){
                        socket = new Socket(host, port);
                    } else {
                        socket = new Socket((String)null, port);
                    }
                    connected = true;
                } catch (ConnectException e){
                    logger.trace("[Node {}] [Connect:Phase 2] Connection fail: {}", myId, e.toString());
                    Thread.sleep(1000);          // Wait 1s to resent
                    logger.trace("[Node {}] [Connect:Phase 2] Retry connecting...", myId);
                }

                session.put(process.getNodeId(), socket);
            }
            logger.trace("[Node {}] [Connect:Phase 2] Connection success! to {}:{}", myId, host, port);

            ObjectOutputStream oos = new ObjectOutputStream(session.get(remoteId).getOutputStream());
            
            /* Send a handshake message to P_i */
            Message msg = new Message(myId, process.getNodeId(), MessageType.HANDSHAKE, "Request");
            oos.writeObject(msg);
            oos.flush();
            outMap.put(remoteId, oos);

            // ObjectInputStream constructor will block until the header has been read.  
            ObjectInputStream ois = new ObjectInputStream(session.get(remoteId).getInputStream());
            inMap.put(remoteId, ois);
            msg = (Message)ois.readObject();
            
            if(msg.getMessageType().equals(MessageType.HANDSHAKE)){
                logger.trace("[Node {}] [Connect:Phase 3] InputStream Setup Success!", myId);
            }
            logger.trace("[Node {}] [Connect:Phase 3] Send {}", myId, msg.toString());
            numRecved++;
        }
        logger.trace("[Node {}] [Connect:Phase 3] Build Channel Done.", myId);
    }
    
    
    /**
     * Close all connection to this node.
     * 
     */
    public synchronized void closeSockets(int myId){
        try {
            Iterator<Map.Entry<Integer, Socket>> it = session.entrySet().iterator();
            while (it.hasNext()){
                Map.Entry<Integer, Socket> entry = it.next();
                if(!entry.getValue().isClosed()){
                    entry.getValue().close();
                }
                it.remove();
            }
        } catch (IOException e){
            logger.error("Exception occured when trying to close socket ", e.getMessage());
        }
        logger.info("[Node {}] All Socket Closed.", myId);
    }
}
