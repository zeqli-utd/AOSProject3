package edu.utdallas.project3.protocol;

import java.util.Arrays;

public class DirectClock {
    public int[] clock;
    int myId;
    
    public DirectClock(int numProc, int id) {
        myId = id;
        clock = new int[numProc];
        clock[myId] = 1;
    }
    
    public int getValue(int i) {
        return clock[i];
    }
    
    public void tick() {
        clock[myId]++;
    }
    
    public void sendAction() {
        // sentValue = clock[myId];
        tick();
    }
    public void receiveAction(int sender, int sentValue) {
        clock[sender] = Math.max(clock[sender], sentValue);
        clock[myId] = Math.max(clock[myId], sentValue) + 1;
    }
    
    public boolean isSmallest(int timestamp){
        for (int i = 0; i < clock.length; i++){
            if (i != myId && timestamp >= getValue(i))
                return false;
        }
        return true;
    }
    
    @Override
    public String toString(){
        return Arrays.toString(clock);
    }
}
