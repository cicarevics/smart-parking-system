package com.smartparking.paymentservice.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads and verifies the Bearer token on every request. Populates the
 * SecurityContext on success; on failure, records why so
 * JsonAuthenticationEntryPoint can report a specific reason instead of a
 * generic 401. Only ever sees driver tokens -- nothing calls into
 * payment-service with an internal token, so unlike reservation-service's
 * filter there's no role claim to check here.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_FAILURE_ATTRIBUTE = "authFailureReason";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String userId = jwtService.extractSubject(token);
                var authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                request.setAttribute(AUTH_FAILURE_ATTRIBUTE, "Token expired");
            } catch (JwtException e) {
                request.setAttribute(AUTH_FAILURE_ATTRIBUTE, "Invalid token");
            }
        }

        filterChain.doFilter(request, response);
    }

    // OncePerRequestFilter skips error dispatches by default. That's wrong
    // here: an uncaught exception (e.g. reservation-service being briefly
    // unresolvable in Eureka) makes Boot forward internally to /error,
    // re-running the whole security filter chain. If this filter skips
    // that second pass, it never re-authenticates, /error gets evaluated
    // as anonymous under anyRequest().authenticated(), and the entry
    // point overwrites the real error with a misleading 401. Running on
    // error dispatches too keeps the driver authenticated on that second
    // pass, so the actual status (500, 502, ...) reaches the client. Same
    // fix as parking-service and reservation-service.
    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
