package edu.utdallas.project3.protocol;

import java.util.LinkedList;
import java.util.Queue;

import edu.utdallas.project3.server.Message;
import edu.utdallas.project3.server.MessageType;
import edu.utdallas.project3.server.Process;
import edu.utdallas.project3.socket.Linker;
import edu.utdallas.project3.tools.MutexConfig;

public class RAMutex extends Process implements Lock {
    int requestTimestamp;
    LamportClock c;
    Queue<Integer> pendingQ;         // Deferred messages
    int numOkay = 0;
    
    public RAMutex(Linker linker, MutexConfig config) {
        super(linker, config);
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
    
    
    public synchronized void sendOkayMessage(int destination, int timestamp) {
        Message message = new Message(myId, destination, MessageType.RA_OK, "Okay");
        message.setTimestamp(timestamp);
        super.sendMessage(destination, message);
    }
    
    public synchronized void broadcastRequestMessage(int timestamp) {
        Message message = new Message(myId, DUMMY_DESTINATION, MessageType.REQUEST, "Request");
        message.setTimestamp(timestamp);
        super.broadcast(message);
    }
    
    public synchronized void broadcastOKayMessage(int timestamp) {
        Message message = new Message(myId, DUMMY_DESTINATION, MessageType.RA_OK, "Okay");
        message.setTimestamp(timestamp);
        super.broadcast(message);
    }
    
    @Override
    public synchronized void handleMessage(Message message, int src, MessageType tag) {
        int timeStamp = message.getTimestamp();
        c.receiveAction(src, timeStamp);
        if (tag.equals(MessageType.REQUEST)) {
            if ((requestTimestamp == INFINITY)            // Pi has no unfulfilled request of its own
                    || (timeStamp < requestTimestamp)
                    || ((timeStamp == requestTimestamp) && (src < myId)))
                sendOkayMessage(src, c.getValue());
            else
                pendingQ.add(src);
        } else if (tag.equals(MessageType.RA_OK)) {
            numOkay++;
            if (numOkay == numProc)
                notify(); // okayCS() may be true now
        }
    }
}