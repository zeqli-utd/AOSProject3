package edu.utdallas.project3.server;

/**
 * Thread listen to each neighbors
 * @author zeqing
 *
 */
public class ListenerThread implements Runnable {
    private int myId;
    private int channel;
    private MessageHandler process;
    
    
    public ListenerThread(int myId, int channel, MessageHandler process) {
        this.myId = myId;
        this.channel = channel;
        this.process = process;
    }


    @Override
    public void run() {
        while(true){
            Message message = process.receiveMessage(channel);
            if (message == null)
                break;
            process.handleMessage(message, message.getSourceId(), message.getMessageType());
        }
      
    }


}
