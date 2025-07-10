package com.ff.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.util.AntPathMatcher;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component

public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private final String secretKey;

    public JwtAuthenticationFilter(@Value("${spring.security.jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PUBLIC_ROUTES = List.of(
            "/api/clients/auth/login",
            "/api/clients/auth/register",
            "/api/clients/auth/refresh",
            "/api/public/products/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        //Autoriser les routes publiques
        boolean isPublic = PUBLIC_ROUTES.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        if (isPublic) {
            log.info("🔓 Public route accessed: {}", path);
            return chain.filter(exchange);
        }

        //Vérifier la présence du token
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            log.warn("Missing Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Invalid Authorization header");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        try {
            Claims claims = Jwts.parserBuilder()
                            .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

            String username = claims.getSubject();
            String roles = claims.get("roles", String.class); // si tu stockes les rôles comme "ADMIN", "USER", etc.
            Object userIdObj = claims.get("userId");
            String userIdStr = userIdObj != null ? userIdObj.toString() : "";

            //Ajout des claims au header pour les microservices
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userIdStr != null ? userIdStr : "")
                    .header("X-Username", username)
                    .header("X-Roles", roles != null ? roles : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (SignatureException | IllegalArgumentException e) {
            log.error("Invalid JWT: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1; // Plus prioritaire que les autres
    }
}
