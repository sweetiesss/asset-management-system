package com.nashtech.rookies.oam.constant;

public final class AdminApiPaths {
    public static final String[] ADMIN_POST_ENDPOINTS = {
            "/api/v1/users",
            "/api/v1/categories",
            "/api/v1/assets",
            "/api/v1/assignments",
    };
    public static final String[] ADMIN_PATCH_ENDPOINTS = {
            "/api/v1/users/**",
            "/api/v1/assets/**",
            "/api/v1/assignments/**",
            "/api/v1/asset-returns/**",
    };

    public static final String[] ADMIN_PUT_ENDPOINTS = {
            "/api/v1/assignments/**",
    };

    public static final String[] ADMIN_GET_ENDPOINTS = {
            "/api/v1/users/{id}",
            "/api/v1/users",
            "/api/v1/categories",
            "/api/v1/assets/**",
            "/api/v1/assignments/*/edit-view",
            "/api/v1/reports",
            "/api/v1/reports/**",
    };

    private AdminApiPaths() {
        // Prevent instantiation
    }
}

