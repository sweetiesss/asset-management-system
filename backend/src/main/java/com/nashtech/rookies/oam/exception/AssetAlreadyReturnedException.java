package com.nashtech.rookies.oam.exception;

public class AssetAlreadyReturnedException extends RuntimeException {
    public AssetAlreadyReturnedException(String message) {
        super(message);
    }
}
