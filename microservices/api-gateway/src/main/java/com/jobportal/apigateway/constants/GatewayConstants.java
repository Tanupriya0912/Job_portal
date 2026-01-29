package com.jobportal.apigateway.constants;

public final class GatewayConstants {

    private GatewayConstants() {}

    // Headers injected by gateway
    public static final String HEADER_USER_ID = "X-USER-ID";
    public static final String HEADER_USER_ROLE = "X-USER-ROLE";

    // Auth cookie
    public static final String COOKIE_NAME = "jobPortalToken";

    /**
     * Public endpoints (NO JWT required)
     * Includes both pre-rewrite and post-rewrite paths
     * Note: /auth/me requires JWT validation to get user info
     */
    public static final String[] PUBLIC_PATHS = {
            // Pre-rewrite
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/logout",
            "/api/v1/health",

            // Post-rewrite
            "/auth/register",
            "/auth/login",
            "/auth/logout",
            "/health"
    };
}
