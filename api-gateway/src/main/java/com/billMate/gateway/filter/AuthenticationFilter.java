package com.billMate.gateway.filter;

import com.billMate.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.debug(">> Incoming request", kv("method", exchange.getRequest().getMethod()), kv("path", path));

        // Rutas públicas
        if (
                path.equals("/auth/login") ||
                        path.equals("/auth/register") ||
                        path.equals("/login") ||
                        path.equals("/") ||
                        path.startsWith("/actuator/") ||
                        path.startsWith("/facturas") ||
                        path.equals("/dashboard") ||
                        path.equals("/clientes") ||
                        path.startsWith("/plugins/") ||
                        path.startsWith("/dist/") ||
                        path.startsWith("/css/") ||
                        path.startsWith("/js/") ||
                        path.startsWith("/clientes/") ||
                        path.startsWith("/facturas-cliente") ||
                        path.equals("/usuarios")
        ) {
            log.debug("Public route, skipping auth", kv("path", path));
            return chain.filter(exchange);
        }


        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid token", kv("method", exchange.getRequest().getMethod()), kv("path", path));
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid token", kv("method", exchange.getRequest().getMethod()), kv("path", path));
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String email = jwtUtil.extractEmail(token);
        List<String> roles = jwtUtil.extractRoles(token);
        log.debug("Token valid", kv("email", email), kv("roles", roles));

        // 🔒 Verificar reglas de acceso
        if (path.startsWith("/billing/") && !(roles.contains("ADMIN") || roles.contains("USER"))) {
            log.warn("Access denied to billing", kv("path", path), kv("email", email), kv("roles", roles));
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        if (path.startsWith("/auth/users") && !roles.contains("ADMIN")) {
            log.warn("Access denied: ADMIN required", kv("path", path), kv("email", email));
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }


        // Crear contexto de seguridad (aunque no se use, se mantiene limpio)
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());


        var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        var context = new SecurityContextImpl(auth);

        log.debug("<< Access granted", kv("method", exchange.getRequest().getMethod()), kv("path", path), kv("email", email));
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
    }
}

