package edu.utdallas.project3.server;

/**
 * Message handling interface
 * @author zeqing
 *
 */
public interface MessageHandler {
    void handleMessage(Message m, int srcId, MessageType tag);
    Message receiveMessage(int fromId);
}
