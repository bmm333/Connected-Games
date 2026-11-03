package com.connectedgames.identity.domain.session;

import java.time.Duration;

public enum SessionKind{
    GUEST(Duration.ofHours(12),false),
    USER(Duration.ofDays(30),true),
    DEVICE(Duration.ofDays(90),true);
    private final Duration refreshLifetime;
    private final boolean extendedOnRefresh;
    SessionKind(Duration refreshLifetime,boolean extendedOnRefresh)
    {
        this.refreshLifetime=refreshLifetime;
        this.extendedOnRefresh=extendedOnRefresh;
    }
    public Duration refreshLifetime()
{
        return refreshLifetime;
    }
    public boolean extendedOnRefresh(){
        return extendedOnRefresh;
    }
}
