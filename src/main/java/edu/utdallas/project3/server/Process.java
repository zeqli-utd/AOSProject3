package edu.utdallas.project3.server;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.utdallas.project3.socket.Linker;
import edu.utdallas.project3.tools.MutexConfig;

public class Process implements MessageHandler{

	private static final Logger logger = LogManager.getLogger(Process.class.getName());
	
    protected int numProc, myId;
    protected Linker linker;
   
    
    /**
     * Central repository to retrieve and store helpful settings
     */
    protected MutexConfig config;    

    public Process(Linker initLinker, MutexConfig config){
        this.config = config;
        
        this.linker = initLinker;
        this.myId = linker.getMyId();
        this.numProc = linker.getNeighbors().size();
    }
    
    /**
     * Default message handler.
     * Accept and process application message only 
     * 
     * Only one thread can handle message at the same time.
     * 
     * @throws IOException 
     */
    public synchronized void handleMessage(Message msg, int srcId, MessageType tag) {
        logger.info("[Node {}] [Request] content={}", myId, msg.toString());
    }

    public void sendMessage(int destination, MessageType tag, String content) {
        Message message = new Message(myId, destination, tag, content);
        linker.sendMessage(destination, message);
    }
   
    public void sendMessage(int destination, Message message) {
        linker.sendMessage(destination, message);
    }
    
    public void broadcast(Message message) {
        linker.broadcast(message);
    }
    
    /**
     * Broadcast messages to all its neighbors
     * @param tag
     * @param content
     * @throws IOException
     */
    public void sendToNeighbors(MessageType tag, String content) {
        List<Node> neighbors = linker.getNeighbors();
        linker.multicast(neighbors, tag, content);
    }
    
    /**
     * Retrieve message for a specific node
     * @throws IOException 
     */
    public Message receiveMessage(int fromId) {
        Message message = linker.receiveMessage(fromId);
        return message;
    }
    
    public synchronized void procWait(){
        try{
            wait();
        } catch (InterruptedException e){
			Thread.currentThread().interrupt();
        }
    }
    
    public int getNumProc() {
		return numProc;
	}

	public int getMyId() {
		return myId;
	}

	public MutexConfig getConfig() {
		return config;
	}

	/**
     * String representation of process
     */
    public synchronized String toString(){
        StringBuilder sb = new StringBuilder();
        String newline = "\n";
        String meta = String.format("[Node Id = %d]", myId);
        sb.append(meta).append(newline);
        if(numProc != 0){
            String neighborInfo = String.format("Neighbor Info: \nnumProc: %d", numProc);
            sb.append(neighborInfo).append(newline);
            List<Node> neighbors = linker.getNeighbors();
            for(Node node : neighbors){
                sb.append(node.toString()).append(newline);
            }
        }
        return sb.toString();
    }
}
