package com.nashtech.rookies.oam.exception;

public class AssetBeingModifiedException extends RuntimeException {
    public AssetBeingModifiedException(String message) {
        super(message);
    }
}
