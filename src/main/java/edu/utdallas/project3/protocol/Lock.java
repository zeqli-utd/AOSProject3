package edu.utdallas.project3.protocol;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.utdallas.project3.server.Message;
import edu.utdallas.project3.server.MessageHandler;
import edu.utdallas.project3.server.MessageType;
import edu.utdallas.project3.server.Process;

public abstract class Lock implements MessageHandler {
	private static final Logger logger = LogManager.getLogger(Lock.class.getName());

	protected static final int INFINITY = -1;
    protected static final int DUMMY_DESTINATION = -1;
    
    protected CountDownLatch isDone;
	protected Process proc;
	protected int numProc, myId;
	
	public Lock(Process proc){
		this.proc = proc;
		this.numProc = proc.getNumProc();
		this.myId = proc.getMyId();
        this.isDone = new CountDownLatch(numProc + 1);
	}
    /**
     * Enter critical section. May block.
     * @throws IOException 
     */
    public abstract void csEnter();      
    
    /**
     * Leave critical section. 
     */
    public abstract void csLeave();

    public void broadcastTerminationMessage() {
    	Message message = new Message(myId, DUMMY_DESTINATION, MessageType.TERMINATE, MessageType.TERMINATE.name());
    	logger.trace("[Node {}] broadcast termination message", myId);
        broadcast(message);
        isDone.countDown();
    }
    
    public void sendMessage(int destination, Message message) {
		proc.sendMessage(destination, message);
	}

	public void broadcast(Message message) {
		proc.broadcast(message);
	}
    
	@Override
	public Message receiveMessage(int fromId) {
		return proc.receiveMessage(fromId);
	}

	public void procWait(){
		try {
			wait();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
    
    public void waitForDone(){
    	try {
			isDone.await();
			logger.info("[Node {}] Terminate.", myId);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
    }
}