package com.nashtech.rookies.oam.exception;

public class InternalErrorException extends RuntimeException {
    public InternalErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
