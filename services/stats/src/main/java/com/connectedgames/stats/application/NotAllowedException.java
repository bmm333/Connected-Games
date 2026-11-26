package com.connectedgames.stats.application;

public final class NotAllowedException extends RuntimeException{
    public NotAllowedException() {
        super("The viewer cannot read this");
    }
}
