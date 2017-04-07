package edu.utdallas.project3.protocol;

public class LamportClock {
    int timestamp;
    public LamportClock() {
        timestamp = 1;
    }
    public int getValue() {
        return timestamp;
    }
    public void tick() { // on internal actions
        timestamp = timestamp + 1;
    }
    public void sendAction() {
       // include c in message
        timestamp = timestamp + 1;
    }
    public void receiveAction(int src, int sentValue) {
        timestamp = Math.max(timestamp, sentValue) + 1;
    }
}
