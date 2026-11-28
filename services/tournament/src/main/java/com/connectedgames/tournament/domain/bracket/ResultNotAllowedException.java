package com.connectedgames.tournament.domain.bracket;

public class ResultNotAllowedException extends RuntimeException{
    public ResultNotAllowedException(String message){
        super(message);
    }
}
