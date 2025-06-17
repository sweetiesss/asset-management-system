package com.nashtech.rookies.oam.exception;

public class RefreshTokenMissingException extends RuntimeException {
    public RefreshTokenMissingException(String message) {
        super(message);
    }
}
