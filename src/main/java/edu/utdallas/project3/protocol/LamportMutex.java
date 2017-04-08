package edu.utdallas.project3.protocol;
import java.io.IOException;

import edu.utdallas.project3.server.Message;
import edu.utdallas.project3.server.MessageType;
import edu.utdallas.project3.server.Process;
import edu.utdallas.project3.socket.Linker;
import edu.utdallas.project3.tools.MutexConfig;

public class LamportMutex extends Process implements Lock {
    
    DirectClock v;
    int[] requestQueue; // request queue
    
    
    public LamportMutex(Linker linker, MutexConfig config) {
        super(linker, config);
        v = new DirectClock(numProc, myId);
        requestQueue = new int[numProc];
        for (int j = 0; j < numProc; j++)
            requestQueue[j] = INFINITY;
    }
    
    
    
    public synchronized void csEnter() throws IOException {
        v.tick();
        requestQueue[myId] = v.getValue(myId);
        broadcastRequestMessage(requestQueue[myId]);
        while (!okayCS())
            procWait();
    }
    
    
    public synchronized void csLeave() throws IOException {
        requestQueue[myId] = INFINITY;
        broadcastReleaseMessage(v.getValue(myId));
    }
    
    
    boolean okayCS() {
        for (int j = 0; j < numProc; j++){
            if (isGreater(requestQueue[myId], myId, requestQueue[j], j))
                return false;
            if (isGreater(requestQueue[myId], myId, v.getValue(j), j))
                return false;
        }
        return true;
    }
    
    
    boolean isGreater(int entry1, int pid1, int entry2, int pid2) {
        if (entry2 == INFINITY) return false;
        return ((entry1 > entry2)
                || ((entry1 == entry2) && (pid1 > pid2)));
    }
    
    public synchronized void sendACKMessage(int destination, int timestamp) throws IOException{
        Message message = new Message(myId, destination, MessageType.ACK, "Acknowledgement");
        message.setTimestamp(timestamp);
        super.sendMessage(destination, message);
    }
    
    public synchronized void broadcastRequestMessage(int timestamp) throws IOException{
        Message message = new Message(myId, DUMMY_DESTINATION, MessageType.REQUEST, "Request");
        message.setTimestamp(timestamp);
        super.broadcast(message);
    }
    
    public synchronized void broadcastReleaseMessage(int timestamp) throws IOException{
        Message message = new Message(myId, DUMMY_DESTINATION, MessageType.RELEASE, "Release");
        message.setTimestamp(timestamp);
        super.broadcast(message);
    }
    
    
    @Override
    public synchronized void handleMessage(Message message, int src, MessageType tag) throws IOException {
        int timeStamp = message.getTimestamp();
        v.receiveAction(src, timeStamp);
        
        if (tag.equals(MessageType.REQUEST)) {
            requestQueue[src] = timeStamp;
            sendACKMessage(src, v.getValue(myId));
            
        } else if (tag.equals(MessageType.RELEASE))
            requestQueue[src] = INFINITY;
        notify(); // okayCS() may be true now
    }
}
