package com.connectedgames.catalog.domain.table;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 *  Token printed on QR, safe on URL, Unique for the whole platform
 * */

public record QrCode(String value) {
    private static final Pattern FORMAT = Pattern.compile("[A-Za-z0-9_-]{8,32}");

    public QrCode {
        Objects.requireNonNull(value,"value");
        if(!FORMAT.matcher(value).matches())
            throw new IllegalArgumentException("QR Code not valid: " + value);
    }
}
