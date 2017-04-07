package edu.utdallas.project3.protocol;

import java.io.IOException;

import edu.utdallas.project3.server.MessageHandler;

public interface Lock extends MessageHandler {
    /**
     * Enter critical section. May block.
     * @throws IOException 
     */
    public void csEnter() throws IOException;      
    
    /**
     * Leave critical section. 
     */
    public void csLeave() throws IOException;
}