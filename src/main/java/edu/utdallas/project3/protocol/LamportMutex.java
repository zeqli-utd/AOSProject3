package edu.utdallas.project3.protocol;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.utdallas.project3.server.Message;
import edu.utdallas.project3.server.MessageType;
import edu.utdallas.project3.server.Process;

public class LamportMutex extends Lock {
    private static final Logger logger = LogManager.getLogger(LamportMutex.class.getName());
    private DirectClock v;
    private PriorityQueue<CritialSectionRequest> requestQueue; 
    
    
    
    public LamportMutex(Process proc) {
        super(proc);
        this.v = new DirectClock(proc.getNumProc() + 1, proc.getMyId());
        this.requestQueue = new PriorityQueue<>(new RequestComparator());
    }    
    
    public synchronized void csEnter() {
        v.tick();
        int myTimestamp = v.getValue(myId);
        requestQueue.offer(new CritialSectionRequest(myId, v.getValue(myId)));
        broadcastRequestMessage(myTimestamp);
        while (!okayCS()){
            procWait();
        }
    }
    
    public synchronized void csLeave() {
        requestQueue.poll();
        broadcastReleaseMessage(v.getValue(myId));
    }
    
    private boolean okayCS() {
        // Pi’s own request is at the top of its queue. Conform to L2
        if (!requestQueue.isEmpty() && requestQueue.peek().nodeId == myId){
            logger.trace("[Node {}] {}", myId, v.toString());
            
            // Pi has received a message with timestamp larger than that of its own request from all processes.
            if (v.isSmallest(requestQueue.peek().timestamp)) {
                return true;
            } 
        }
        return false;
    }
    
    public void sendACKMessage(int destination, int timestamp) {
        Message message = new Message(myId, destination, MessageType.ACK, "Acknowledgement");
        message.setTimestamp(timestamp);
        sendMessage(destination, message);
    }
    
    public void broadcastRequestMessage(int timestamp) {
        Message message = new Message(myId, DUMMY_DESTINATION, MessageType.REQUEST, "Request");
        message.setTimestamp(timestamp);
        broadcast(message);
    }
    
    public void broadcastReleaseMessage(int timestamp) {
        Message message = new Message(myId, DUMMY_DESTINATION, MessageType.RELEASE, "Release");
        message.setTimestamp(timestamp);
        broadcast(message);
    }
    
    
    
    @Override
    public synchronized void handleMessage(Message message, int srcId, MessageType tag) {
        int timeStamp = message.getTimestamp();
        v.receiveAction(srcId, timeStamp);
        switch (tag){
        	case REQUEST:
        		requestQueue.offer(new CritialSectionRequest(srcId, timeStamp));
                sendACKMessage(srcId, v.getValue(myId));
                break;
        	case RELEASE:
        		requestQueue.poll();
        		break;
        	case TERMINATE:
        		long receivedCount = numProc + 1 - isDone.getCount();
            	logger.info("[Node {}] Terminate message from {} ({}/{})", myId, srcId, receivedCount, numProc + 1);
            	isDone.countDown();
            default:
            	break;
        }
        notify(); // okayCS() may be true now
    }
    
    class RequestComparator implements Comparator<CritialSectionRequest>{
		@Override
		public int compare(CritialSectionRequest o1, CritialSectionRequest o2){
            if (o1.timestamp != o2.timestamp){
                return o1.timestamp - o2.timestamp;
            } else {
                return o1.nodeId - o2.nodeId;
            }
        }
    }
    
    class CritialSectionRequest{
        int nodeId;
        int timestamp;
        
        public CritialSectionRequest(int id, int timestamp){
            this.nodeId = id;
            this.timestamp = timestamp;
        }
    }
}
