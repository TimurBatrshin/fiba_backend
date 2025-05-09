package com.fiba.api.config;

import com.fiba.api.security.JwtAuthenticationFilter;
import com.fiba.api.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.List;

/**
 * Конфигурация для настройки иерархии ролей и дополнительных разрешений безопасности
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8099", 
            "https://dev.bro-js.ru",
            "https://timurbatrshin-fiba-backend-fc1f.twc1.net",
            "https://timurbatrshin-fiba-backend-5ef6.twc1.net",
            "http://localhost:3000",
            "http://localhost"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Разрешаем доступ к файлам без авторизации
                .requestMatchers("/api/files/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                // Разрешаем доступ только к статическим ресурсам и публичным API
                .requestMatchers("/static/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/status").permitAll()
                .requestMatchers("/api/tournaments").permitAll()
                .requestMatchers("/api/tournaments/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/tournaments").permitAll()
                .requestMatchers("/api/ads/public/**").permitAll()
                .requestMatchers("/api/users/search").permitAll()
                .requestMatchers("/api/users/**").permitAll()
                .requestMatchers("/api/profile/**").permitAll()
                .requestMatchers("/api/players/search").permitAll()
                .requestMatchers("/api/players/rankings").permitAll()
                .requestMatchers("/api/players/top").permitAll()
                .requestMatchers("/api/proxy/**").permitAll()
                // Swagger UI и OpenAPI
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                // Разрешаем OPTIONS запросы для CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint((request, response, authException) -> {
                    String path = request.getRequestURI();
                    // Не отправляем 401 для запросов к статическим файлам и OPTIONS запросов
                    if (!path.startsWith("/static/") && 
                        !path.startsWith("/api/auth/") &&
                        !path.startsWith("/api/public/") &&
                        !path.startsWith("/api/files/") &&
                        !path.startsWith("/uploads/") &&
                        !request.getMethod().equals("OPTIONS")) {
                        response.sendError(401, authException.getMessage());
                    }
                })
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
} 