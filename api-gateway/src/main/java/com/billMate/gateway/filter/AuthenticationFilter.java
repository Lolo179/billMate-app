package com.billMate.gateway.filter;

import com.billMate.gateway.util.JwtUtil;
import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        System.out.println("\n🌐 [Gateway] Nueva solicitud a: " + path);

        // Rutas públicas
        if (
                path.equals("/auth/login") ||
                        path.equals("/auth/register") ||
                        path.equals("/login") ||
                        path.equals("/") ||
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
            System.out.println("🔓 Ruta pública permitida sin autenticación.");
            System.out.println("✅ Pasando al microservicio... Ruta final: " + path);
            return chain.filter(exchange);
        }


        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("⛔ No se envió token o formato inválido.");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        System.out.println("🔐 Token recibido (parcial): " + token.substring(0, Math.min(20, token.length())) + "...");

        if (!jwtUtil.isTokenValid(token)) {
            System.out.println("❌ Token inválido.");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String email = jwtUtil.extractEmail(token);
        List<String> roles = jwtUtil.extractRoles(token);
        System.out.println("✅ Token válido. Usuario: " + email);
        System.out.println("🎭 Roles extraídos: " + roles);

        // 🔒 Verificar reglas de acceso
        if (path.startsWith("/billing/") && !(roles.contains("ADMIN") || roles.contains("USER"))) {
            System.out.println("🚫 Acceso denegado: el usuario no tiene permiso para /billing/");
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        if (path.startsWith("/auth/users") && !roles.contains("ADMIN")) {
            System.out.println("🚫 Acceso denegado: solo ADMIN puede acceder a /auth/users");
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }


        // Crear contexto de seguridad (aunque no se use, se mantiene limpio)
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());


        var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
        var context = new SecurityContextImpl(auth);

        System.out.println("✅ Acceso concedido. Continuando hacia el microservicio...");
        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
    }
}

