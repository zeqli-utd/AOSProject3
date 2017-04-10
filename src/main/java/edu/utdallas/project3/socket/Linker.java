package edu.utdallas.project3.socket;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.utdallas.project3.server.Message;
import edu.utdallas.project3.server.MessageType;
import edu.utdallas.project3.server.Node;

/**
 * A coordinator for manage  
 * @author zeqing
 *
 */
public class Linker {
    private static final Logger logger = LogManager.getLogger();

    private int myId;               // Local node id
    private int numProc;            // Number of processes it contact with
    private Connector connector;    
    private List<Node> neighbors;
    
    
    private Map<Integer, ObjectOutputStream> outMap;
    private Map<Integer, ObjectInputStream> inMap;
    
    public Linker(int myId, List<Node> neighbors){
        this.myId = myId;
        this.numProc = neighbors.size();
        this.neighbors = neighbors;
        
        this.inMap = new HashMap<>();
        this.outMap = new HashMap<>();
        this.connector = Connector.getInstance();
    }
    
    /**
     * Build bidirectional channels with all neighbors
     * @param listenPort
     * @throws InterruptedException 
     * @throws IOException 
     * @throws ClassNotFoundException 
     * @throws Exception
     */
    public void buildChannels(int listenPort)  {
        try {
            connector.connect(listenPort, myId, inMap, outMap, neighbors);
        } catch (ClassNotFoundException e){
            logger.error("(Message) Class not found", e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Connection Error ", e.getMessage());
            close();
        } catch (InterruptedException ex){
            Thread.currentThread().interrupt();
            logger.error("Thread Error ", ex.getMessage());
        } 
    }
    
    /**
     * Send one message to a destination neighbor
     * 
     * @param dstId Destination id
     * @param tag Message type
     * @param content Message body
     * @throws IOException 
     */
    public synchronized void sendMessage(int dstId, MessageType tag, String content) {
        sendMessage(dstId, new Message(myId, dstId, tag, content));
    }
    
    public synchronized void sendMessage(int dstId, Message message) {
        try {
            outMap.get(dstId).writeObject(message);
        } catch (IOException e){
            logger.error("Connection Error ", e.getMessage());
        } 
        
    }
    
    /**
     * Multicast to a group of destinations
     * 
     * @param members The multicast group member
     * @param tag Message type
     * @param content Message body
     * @throws IOException
     */
    public void multicast(List<Node> members, MessageType tag, String content) {
        for(Node member : members){
            sendMessage(member.getNodeId(), tag, content);
        }
    }
    
    public synchronized void broadcast(Message message) {
        if (neighbors != null && !neighbors.isEmpty()){
            for (Node remote : neighbors){
                int destinationId = remote.getNodeId();
                message.setDestinationId(destinationId);
                sendMessage(destinationId, message);
            }
        }
    }
        
    /**
     * Listen to a particular neighbor
     * 
     * @param fromId
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public Message receiveMessage(int fromId){
        Message message = null;
        try {
            message = (Message)inMap.get(fromId).readObject(); // This will block if no message.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.print(String.format("[Node %d] Channel %d Terminated. %s\n", myId, fromId, e.toString()));
            inMap.remove(fromId);
        }
        return message;
    }
    
    public int getMyId() {
        return myId;
    }

    public void setMyId(int myId) {
        this.myId = myId;
    }

    public List<Node> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<Node> neighbors) {
        this.neighbors = neighbors;
    }

    public void close(){
        connector.closeSockets(myId);
    }
    
    public int getNumProc(){
    	return this.numProc;
    }
}
