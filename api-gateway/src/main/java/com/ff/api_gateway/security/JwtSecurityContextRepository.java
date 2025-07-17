package com.ff.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, org.springframework.security.core.context.SecurityContext context) {
        return Mono.empty(); // inutile ici
    }

    @Override
    public Mono<org.springframework.security.core.context.SecurityContext> load(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String userId = String.valueOf(claims.get("userId", Long.class));
                String role = claims.get("role", String.class);
                System.out.println("User ID: " + userId);
                System.out.println("Role: " + role);
                List<GrantedAuthority> authorities = List.of(() -> "ROLE_" + role);

                Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);

                return Mono.just(new SecurityContextImpl(auth));

            } catch (Exception e) {
                return Mono.empty(); // Token invalide
            }
        }

        return Mono.empty(); // Pas de header
    }
}
