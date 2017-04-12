package edu.utdallas.project3.protocol;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.utdallas.project3.server.Message;
import edu.utdallas.project3.server.MessageType;
import edu.utdallas.project3.server.Process;

public class RAMutex extends Lock {
	private static final Logger logger = LogManager.getLogger(RAMutex.class.getName());
    int requestTimestamp;
    LamportClock c;
    Queue<Integer> pendingQ;         // Deferred messages
    int numOkay = 0;
    
    public RAMutex(Process proc) {
        super(proc);
        requestTimestamp = INFINITY;
        pendingQ = new LinkedList<>();
        c =  new LamportClock();
    }
    
    @Override
    public synchronized void csEnter() {
        c.tick();
        requestTimestamp = c.getValue();
        broadcastRequestMessage(requestTimestamp);
        numOkay = 0;
        while (numOkay < numProc)
            procWait();
    }
    public synchronized void csLeave() {
        requestTimestamp = INFINITY;
        while (!pendingQ.isEmpty()) {
            int pid = pendingQ.poll();
            sendOkayMessage(pid, c.getValue());
        }
    }
    
    private void sendOkayMessage(int destination, int timestamp) {
        Message message = new Message(myId, destination, MessageType.RA_OK, "Okay");
        message.setTimestamp(timestamp);
        sendMessage(destination, message);
    }

	private void broadcastRequestMessage(int timestamp) {
        Message message = new Message(myId, DUMMY_DESTINATION, MessageType.REQUEST, "Request");
        logger.trace("[Node {}] broadcast request message", myId);
        message.setTimestamp(timestamp);
        broadcast(message);
    }
    
    @Override
    public synchronized void handleMessage(Message message, int srcId, MessageType tag) {
        int timeStamp = message.getTimestamp();
        c.receiveAction(srcId, timeStamp);
        switch (tag){
        	case REQUEST:	
                if ((requestTimestamp == INFINITY)            // Pi has no unfulfilled request of its own
                        || (timeStamp < requestTimestamp)
                        || ((timeStamp == requestTimestamp) && (srcId < myId)))
                    sendOkayMessage(srcId, c.getValue());
                else
                    pendingQ.add(srcId);
                break;
        	case RA_OK:
                numOkay++;
                if (numOkay == numProc)
                    notify(); // okayCS() may be true now
                break;
        	case TERMINATE:
        		long receivedCount = numProc + 1 - isDone.getCount();
            	logger.info("[Node {}] Terminate message from {} ({}/{})", myId, srcId, receivedCount, numProc + 1);
            	isDone.countDown();
        	default:
        		break;
        	
        }
    }
}