package com.nashtech.rookies.oam.exception;

public class OldPasswordNotMatchException extends RuntimeException{
    public OldPasswordNotMatchException(String message) {
        super(message);
    }

}
