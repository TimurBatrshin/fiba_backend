package com.fiba.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = getJwtFromRequest(request);
            log.debug("JWT токен из запроса: {}", token != null ? "получен" : "отсутствует");
            
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                try {
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Аутентификация успешно установлена в SecurityContext для пути: {}", request.getRequestURI());
                } catch (UsernameNotFoundException ex) {
                    log.error("Не удалось установить аутентификацию пользователя: пользователь не найден", ex);
                    SecurityContextHolder.clearContext();
                } catch (Exception ex) {
                    log.error("Не удалось установить аутентификацию пользователя: {}", ex.getMessage(), ex);
                    SecurityContextHolder.clearContext();
                }
            } else if (StringUtils.hasText(token)) {
                log.warn("Полученный JWT токен недействителен для пути: {}", request.getRequestURI());
            }
        } catch (Exception ex) {
            log.error("Не удалось обработать JWT токен: {}", ex.getMessage(), ex);
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.debug("Authorization header: {}", bearerToken != null ? (bearerToken.length() > 15 ? 
                  bearerToken.substring(0, 15) + "..." : bearerToken) : "отсутствует");
                  
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 