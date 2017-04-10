package edu.utdallas.project3.protocol;

import java.io.IOException;

import edu.utdallas.project3.server.MessageHandler;

public interface Lock extends MessageHandler {
    /**
     * Enter critical section. May block.
     * @throws IOException 
     */
    void csEnter();      
    
    /**
     * Leave critical section. 
     */
    void csLeave();
}