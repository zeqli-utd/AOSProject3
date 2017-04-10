package edu.utdallas.project3.protocol;
import java.util.Comparator;
import java.util.PriorityQueue;

import edu.utdallas.project3.server.Message;
import edu.utdallas.project3.server.MessageType;
import edu.utdallas.project3.server.Process;
import edu.utdallas.project3.socket.Linker;
import edu.utdallas.project3.tools.MutexConfig;

public class LamportMutex extends Process implements Lock {
    
    DirectClock v;
    PriorityQueue<CritialSectionRequest> requestQueue; 
    
    
    public LamportMutex(Linker linker, MutexConfig config) {
        super(linker, config);
        v = new DirectClock(numProc + 1, myId);
        requestQueue = new PriorityQueue<>(new Comparator<CritialSectionRequest>(){
            public int compare(CritialSectionRequest o1, CritialSectionRequest o2){
                if (o1.timestamp != o2.timestamp){
                    return o1.timestamp - o2.timestamp;
                } else {
                    return o1.nodeId - o2.nodeId;
                }
            }
        });
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
            System.out.print(String.format("[Node %d] %s\n", myId, v.toString()));
            
            // Pi has received a message with timestamp larger than that of its own request from all processes.
            if (v.isSmallest(requestQueue.peek().timestamp)) {
                return true;
            } 
        }
        return false;
    }
    
    public synchronized void sendACKMessage(int destination, int timestamp) {
        Message message = new Message(myId, destination, MessageType.ACK, "Acknowledgement");
        message.setTimestamp(timestamp);
        super.sendMessage(destination, message);
    }
    
    public synchronized void broadcastRequestMessage(int timestamp) {
        Message message = new Message(myId, DUMMY_DESTINATION, MessageType.REQUEST, "Request");
        message.setTimestamp(timestamp);
        super.broadcast(message);
    }
    
    public synchronized void broadcastReleaseMessage(int timestamp) {
        Message message = new Message(myId, DUMMY_DESTINATION, MessageType.RELEASE, "Release");
        message.setTimestamp(timestamp);
        super.broadcast(message);
    }
    
    
    @Override
    public synchronized void handleMessage(Message message, int src, MessageType tag) {
        int timeStamp = message.getTimestamp();
        v.receiveAction(src, timeStamp);
        System.out.print(String.format("[Node %d] Accepted Message %s from %d\n", myId, tag.name(), src));
        if (tag.equals(MessageType.REQUEST)) {
            requestQueue.offer(new CritialSectionRequest(src, timeStamp));
            sendACKMessage(src, v.getValue(myId));
            
        } else if (tag.equals(MessageType.RELEASE)){
            requestQueue.poll();
        }
        notify(); // okayCS() may be true now
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
