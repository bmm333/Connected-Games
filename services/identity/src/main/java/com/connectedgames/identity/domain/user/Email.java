package com.connectedgames.identity.domain.user;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern FORMAT=Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    public Email{
        Objects.requireNonNull(value,"value");
        value=value.strip().toLowerCase(Locale.ROOT);
        if(value.length()>254 || !FORMAT.matcher(value).matches())
            throw new IllegalArgumentException("Email not Valid: " + value);
    }
}
