package com.nashtech.rookies.oam.exception;

public class UserBeingModifiedException extends RuntimeException {
    public UserBeingModifiedException(String message) {
        super(message);
    }
}
