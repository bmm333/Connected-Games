package com.connectedgames.identity.domain.user;

import java.util.Objects;

/** Hash of the password (bcrypt): the domain never sees the password in clear */
public record PasswordHash(String value) {
    public PasswordHash{
        Objects.requireNonNull(value,"value");
        if(value.isBlank())
            throw new IllegalArgumentException("Password hash cannot be blank");
    }

    /** Avoiding default tostring since it would print out the user pw  */
    @Override
    public String toString()
    {
        return "PasswordHash:[*******]";
    }
}
