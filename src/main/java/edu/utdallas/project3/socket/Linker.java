package edu.utdallas.project3.socket;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

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
    
    private ObjectOutputStream[] out;
    private ObjectInputStream[] in;
    private int myId;               // Local node id
    private int numProc;            // Number of processes it contact with
    private Connector connector;    
    private List<Node> neighbors;
    
    public Linker(int myId, List<Node> neighbors){
        this.myId = myId;
        this.numProc = neighbors.size();
        this.neighbors = neighbors;
        
        this.out = new ObjectOutputStream[numProc];
        this.in = new ObjectInputStream[numProc];
        
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
            connector.connect(listenPort, myId, in, out, neighbors);
        } catch (ClassNotFoundException e){
            logger.error("(Message) Class not found", e.getMessage());
            close();
        } catch (IOException e) {
            // Prints message and stack trace
            logger.error("Connection Error ", e.getMessage());
            close();
        } catch (InterruptedException ex){
            // restore interrupted status
            Thread.currentThread().interrupt();
            logger.error("Thread Error ", ex.getMessage());
            close();
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
            int dstIndex = idToIndex(dstId);
            out[dstIndex].writeObject(message);
        } catch (IOException e){
            // Prints message and stack trace
            logger.error("Connection Error ", e.getMessage());
            close();
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
    public Message receiveMessage(int fromId) throws IOException, ClassNotFoundException {
        int fromIndex = idToIndex(fromId);
        Message msg = (Message)in[fromIndex].readObject();   // This will block if no message.
        return msg;
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
    
    private int idToIndex(int nodeId){
        return Collections.binarySearch(neighbors, new Node(nodeId));
    }

    public void close(){
        connector.closeSockets(myId);
    }
    
    public int getNumProc(){
    	return this.numProc;
    }
}
