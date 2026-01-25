package org.example.backendspring.Configuration;

import io.jsonwebtoken.Claims;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TokenFilter.class);

    private final JwtCore jwtCore;

    private static final Logger timingLogger = LoggerFactory.getLogger("TimingLogger");




    public TokenFilter(@NonNull JwtCore jwtCore) {
        this.jwtCore = jwtCore;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")||
                path.startsWith("/auth/") ||
                path.startsWith("/signup-Login")||
                path.startsWith("/oauth2/") ||
                path.startsWith("/api/login/oauth2/") ||
                path.startsWith("/api/oauth2/") ||
                path.startsWith("/login/oauth2/") ||
                path.contains("/actuator") ||
                "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Читаем заголовок Authorization
            String header = request.getHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
                return;
            }

            String token = header.substring(7);
            // Проверка токена
            if (!jwtCore.isValidToken(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }

            // Извлекаем данные из токена
            Claims claims = jwtCore.getAllClaimsFromToken(token);
            MyUserDetails userDetails = MyUserDetails.fromClaims(claims);
            if (userDetails == null || userDetails.getUsername()==null||
                    userDetails.getUsername().isBlank()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token (no subject)");
                return;
            }

            // Берём роли только из токена
            @SuppressWarnings("unchecked")
            List<String> rolesFromClaims = (List<String>) claims.get("roles");
            List<String> roles = rolesFromClaims != null ? rolesFromClaims : Collections.emptyList();

            List<GrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // Создаём Authentication и кладём в SecurityContext
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            log.error("TokenFilter error", ex);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
        } finally {
            long totalTime = System.currentTimeMillis() - startTime;
            timingLogger.info("Total request time for {} {}: {} ms",
                    request.getMethod(), request.getRequestURI(), totalTime);
        }
    }
}

