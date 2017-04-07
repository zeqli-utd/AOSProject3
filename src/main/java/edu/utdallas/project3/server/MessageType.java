package edu.utdallas.project3.server;

/**
 * Tag enum for message 
 * @author zeqing
 *
 */
public enum MessageType {
    DEFAULT,
    HANDSHAKE,
    ACK,
    RA_OK,                // Ricart and Agrawala's Ok message
    REQUEST,
    RELEASE
}
