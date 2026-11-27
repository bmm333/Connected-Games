package com.connectedgames.stats.domain.stats;

public record Page(int limit,int offset) {
    public static final int MAX_LIMIT =100;
    public static final int DEFAULT_LIMIT = 20;
    public Page{
        if(limit < 1 || limit > MAX_LIMIT){
            throw new IllegalArgumentException("Limit is between 1 and " + MAX_LIMIT);
        }
        if(offset<0){
            throw new IllegalArgumentException("offset cannot be negative");
        }
    }
    public static Page first(){
        return new Page(DEFAULT_LIMIT,0);
    }
}
