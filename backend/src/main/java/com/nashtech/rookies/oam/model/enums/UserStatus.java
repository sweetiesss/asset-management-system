package com.nashtech.rookies.oam.model.enums;

public enum UserStatus {
    ACTIVE,
    INACTIVE,
    FIRST_LOGIN;

    public boolean isFirstLogin() {
        return this == FIRST_LOGIN;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isInActive() {
        return this == INACTIVE;
    }
}
