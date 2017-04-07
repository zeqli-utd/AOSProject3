package edu.utdallas.project3.server;

import java.io.Serializable;

/**
 * 
 * @author Zeqing Li, zxl165030, The University of Texas at Dallas
 *
 */
public class Message implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private int sourceId;
    private int destinationId;
    private String content;
    private MessageType tag;
    private int timestamp;
    
    /**
     * Constructor for application message
     * 
     * @param srcId
     * @param dst
     * @param content
     */
    public Message(int srcId, int dst, String content){
        this.sourceId = srcId;
        this.destinationId = dst;
        this.tag = MessageType.DEFAULT;
        this.content = content;
    }
    
    /**
     * Constructor for special message, for example, marker, vector, handshake etc
     * @param src
     * @param dst
     * @param tag
     * @param content
     */
    public Message(int src, int dst, MessageType tag, String content){
        this(src, dst, content);
        setMessageType(tag);
    }
    
    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getMessageType() {
        return tag;
    }

    public void setMessageType(MessageType tag) {
        this.tag = tag;
    }
    

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    @Override 
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] SOURCE = %d DST = %d CONTENT = \"%s\" ", 
                tag, this.sourceId, this.destinationId, this.content));
        return sb.toString();
    }


}