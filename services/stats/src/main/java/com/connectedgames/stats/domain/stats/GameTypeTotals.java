package com.connectedgames.stats.domain.stats;

import java.util.Objects;

import com.connectedgames.stats.domain.GameTypeId;

public record GameTypeTotals(GameTypeId gameTypeId,Totals totals) {
    public GameTypeTotals{
        Objects.requireNonNull(gameTypeId,"gameTypeId");
        Objects.requireNonNull(totals,"totals");
    }
}
