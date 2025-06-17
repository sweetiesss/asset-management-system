package com.nashtech.rookies.oam.model.enums;

import lombok.Getter;

@Getter
public enum LocationCode {
    HCM("HCM"),
    HN("HN"),
    DN("DN");

    private final String name;

    LocationCode(String name) {
        this.name = name;
    }
}
