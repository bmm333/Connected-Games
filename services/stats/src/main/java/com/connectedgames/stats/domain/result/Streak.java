package com.connectedgames.stats.domain.result;

import java.util.Objects;
import com.connectedgames.stats.domain.Side;

public record Streak(Side side,int length) {
    public Streak{
        Objects.requireNonNull(side,"side");
        if(length<1)
        {
            throw new IllegalArgumentException("A streak has at least one point");
        }
    }
}
