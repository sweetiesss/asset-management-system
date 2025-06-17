package com.nashtech.rookies.oam.constant;

import java.util.Map;

public class UserConstants {
    private UserConstants() {}

    public static final Map<String, String> USER_SORT_FIELD_MAPPING = Map.of(
            "staffCode", "staffCode",
            "joinedDate", "joinedOn",
            "fullName", "firstName",
            "type", "roles.name"
    );

    public static final Map<String, String> ASSET_SORT_FIELD_MAPPING = Map.of(
            "assetCode", "assetCode",
            "assetName", "assetName",
            "category", "category",
            "state", "state"
    );

    public static final String DEFAULT_PASSWORD_FORMAT = "%s@%s";
}
