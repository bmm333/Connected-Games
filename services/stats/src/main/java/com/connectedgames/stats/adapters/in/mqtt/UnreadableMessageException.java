package com.connectedgames.stats.adapters.in.mqtt;



/**A match complted that cannot be read. It will never be readable so it is dropped.*/
final class UnreadableMessageException extends RuntimeException{
    UnreadableMessageException(String message){
        super(message);
    }
}
