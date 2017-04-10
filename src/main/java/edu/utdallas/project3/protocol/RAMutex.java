package edu.utdallas.project3.protocol;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import edu.utdallas.project3.server.Message;
import edu.utdallas.project3.server.MessageType;
import edu.utdallas.project3.server.Process;
import edu.utdallas.project3.socket.Linker;
import edu.utdallas.project3.tools.MutexConfig;

public class RAMutex extends Process implements Lock {
    int myTimestamp;
    LamportClock c = new LamportClock();
    Queue<Integer> pendingQ = new LinkedList<>();
    int numOkay = 0;
    public RAMutex(Linker linker, MutexConfig config) {
        super(linker, config);
        myTimestamp = INFINITY;
    }
    
    @Override
    public synchronized void csEnter() {
        c.tick();
        myTimestamp = c.getValue();
        broadcastRequestMessage(myTimestamp);
        numOkay = 0;
        while (numOkay < numProc-1)
            procWait();
    }
    public synchronized void csLeave() {
        myTimestamp = INFINITY;
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
    public synchronized void handleMessage(Message message, int src, MessageType tag) throws IOException {
        int timeStamp = message.getTimestamp();
        c.receiveAction(src, timeStamp);
        if (tag.equals(MessageType.REQUEST)) {
            if ((myTimestamp == INFINITY) // not interested in CS
                    || (timeStamp < myTimestamp)
                    || ((timeStamp == myTimestamp) && (src < myId)))
                sendOkayMessage(src, c.getValue());
            else
                pendingQ.add(src);
        } else if (tag.equals(MessageType.RA_OK)) {
            numOkay++;
            if (numOkay == numProc - 1)
                notify(); // okayCS() may be true now
        }
    }
}