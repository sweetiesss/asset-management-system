package com.nashtech.rookies.oam.model.enums;

import lombok.Getter;

@Getter
public enum RoleName {
    ADMIN("ADMIN"),
    STAFF("STAFF");

    private final String name;

    RoleName(String name) {
        this.name = name;
    }
}
