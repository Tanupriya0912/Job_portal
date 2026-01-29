package com.jobportal.apigateway.filter;

import com.jobportal.apigateway.constants.GatewayConstants;
import com.jobportal.apigateway.security.JwtValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class JwtValidationFilter
        extends AbstractGatewayFilterFactory<JwtValidationFilter.Config> {

    private final JwtValidator jwtValidator;

    public JwtValidationFilter(JwtValidator jwtValidator) {
        super(Config.class);
        this.jwtValidator = jwtValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            log.info("[JWT Filter] Processing request: {} {}", request.getMethod(), path);

            // 1️⃣ Allow preflight
            if (request.getMethod() == HttpMethod.OPTIONS) {
                log.info("[JWT Filter] Allowing OPTIONS request");
                return chain.filter(exchange);
            }

            // 2️⃣ Allow public endpoints
            if (isPublicPath(path)) {
                log.info("[JWT Filter] Public path, skipping JWT validation: {}", path);
                return chain.filter(exchange);
            }

            log.info("[JWT Filter] Protected path, extracting JWT from cookie");

            // 3️⃣ Extract JWT
            String token = extractTokenFromCookie(request);
            if (token == null || token.isBlank()) {
                log.warn("[JWT Filter] No JWT token found in cookie for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            log.info("[JWT Filter] JWT token extracted successfully");

            // 4️⃣ Validate JWT
            if (!jwtValidator.isTokenValid(token)) {
                log.warn("[JWT Filter] JWT validation failed for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            log.info("[JWT Filter] JWT validation successful");

            // 5️⃣ Enrich request
            String userId = jwtValidator.getUserIdFromToken(token);
            String role = jwtValidator.getRoleFromToken(token);

            log.info("[JWT Filter] Adding headers - X-USER-ID: {}, X-USER-ROLE: {}", userId, role);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(GatewayConstants.HEADER_USER_ID, userId)
                    .header(GatewayConstants.HEADER_USER_ROLE, role)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private boolean isPublicPath(String path) {
        List<String> publicPaths = List.of(
                "/api/v1/auth/register",
                "/api/v1/auth/login",
                "/api/v1/auth/logout",
                "/auth/register",
                "/auth/login",
                "/auth/logout",
                "/health"
        );
        return publicPaths.stream().anyMatch(path::startsWith);
    }

    private String extractTokenFromCookie(ServerHttpRequest request) {
        var cookies = request.getCookies().get(GatewayConstants.COOKIE_NAME);
        if (cookies != null && !cookies.isEmpty()) {
            return cookies.get(0).getValue();
        }
        return null;
    }

    public static class Config {
    }
}
