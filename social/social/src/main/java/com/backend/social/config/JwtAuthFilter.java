package com.backend.social.config;

import com.backend.social.security.UserPrincipal;
import com.backend.social.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {


    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getRequestURI();
        boolean skip = path.startsWith("/api/auth/");

        if (skip && log.isDebugEnabled()) {
            log.debug("Skipping JWT filter for public endpoint: {}", path);
       }
       return skip;

    }



    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        try {
            String header = request.getHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                chain.doFilter(request, response);
                return;
            }

                String token = header.substring(7);
                String username = jwtUtil.extractUsername(token);
                Long userId = jwtUtil.extractUserId(token);
                String role = jwtUtil.extractRole(token);

                List<GrantedAuthority> authorities =
                        List.of(new SimpleGrantedAuthority("ROLE_" + role));

                // Create custom principal
                UserPrincipal principal = new UserPrincipal(userId, username,role);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                authorities
                        );

               // authentication.setDetails(request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            if (log.isDebugEnabled()) {
                log.debug(
                        "JWT authentication successful for userId={}, role={}",
                        userId,
                        role
                );
            }

        } catch (Exception e) {

            log.warn(
                    "JWT authentication failed for request URI={}",
                    request.getRequestURI()
            );

            if (log.isDebugEnabled()) {
                log.debug("JWT parsing exception details", e);
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
    }
}
