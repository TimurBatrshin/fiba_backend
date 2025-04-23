package com.fiba.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "https://timurbatrshin-fiba-backend-0722.twc1.net",
                    "https://timurbatrshin-fiba-backend-1aa7.twc1.net", 
                    "https://timurbatrshin-fiba-backend-95ba.twc1.net", 
                    "http://localhost:8099", 
                    "https://dev.bro-js.ru"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        config.addAllowedOrigin("https://timurbatrshin-fiba-backend-0722.twc1.net");
        config.addAllowedOrigin("https://timurbatrshin-fiba-backend-1aa7.twc1.net");
        config.addAllowedOrigin("https://timurbatrshin-fiba-backend-95ba.twc1.net");
        config.addAllowedOrigin("http://localhost:8099");
        config.addAllowedOrigin("https://dev.bro-js.ru");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
} 