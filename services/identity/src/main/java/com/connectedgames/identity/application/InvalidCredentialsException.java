package com.connectedgames.identity.application;



/** Wrong email or password; the web adapter maps it to 401 */
public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException()
    {
        super("Wrong email or password");
    }
}
