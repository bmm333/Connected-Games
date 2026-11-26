package com.connectedgames.stats.application.port.in;

/**What happend to a match completed. it was saved, it was already saved, or is it not counted*/
public enum RecordOutcome{
    RECORDED,DUPLICATE,IGNORED
}
