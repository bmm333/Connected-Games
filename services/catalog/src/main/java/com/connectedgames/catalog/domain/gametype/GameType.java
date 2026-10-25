package com.connectedgames.catalog.domain.gametype;

import java.util.Objects;

public record GameType(GameTypeId id,String name,GameMode mode,int participantsPerSide,int pointsToWin) {

    public GameType{
        Objects.requireNonNull(id,"id");
        Objects.requireNonNull(mode,"mode");
        if(name==null||name.isBlank()) throw new IllegalArgumentException("Name of the game is mandatory");
        if(pointsToWin<1) throw new IllegalArgumentException("Points to win cannot be less than 1");
        if(mode==GameMode.INDIVIDUAL && participantsPerSide!=1)
        {
            throw new IllegalArgumentException("Maximum participant per side on Individual game modes is 1");
        }
        if(mode==GameMode.TEAM&&participantsPerSide<=1)
        {
            throw new IllegalArgumentException("When on a team game mode, is required at least 2 participants per side");
        }

    }
}
