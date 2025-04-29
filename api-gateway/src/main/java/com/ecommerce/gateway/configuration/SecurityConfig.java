package com.ecommerce.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List; // Import List

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Disable CSRF as it's typically handled differently in gateways/APIs
                .authorizeExchange(exchanges -> exchanges
                        // --- Public endpoints defined in application.yaml will be handled by AuthenticationFilter ---
                        // --- Let AuthenticationFilter handle public path checks based on config ---
                         .pathMatchers("/api/identity/auth/**").permitAll() // Handled by AuthenticationFilter
                         .pathMatchers("/api/identity/user/create").permitAll() // Example if public product paths exist - Handled by AuthenticationFilter
                        .pathMatchers("/api/search/**").permitAll()
                        .pathMatchers("/api/recombee/**").permitAll()
                        // --- Allow OPTIONS requests for CORS preflight ---
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // --- Admin-only endpoints ---
                        .pathMatchers(HttpMethod.DELETE, "/api/identity/users/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/product/create").hasRole("ADMIN") // Added from AuthenticationFilter
                        .pathMatchers(HttpMethod.PUT, "/api/product/update").hasRole("ADMIN") // Added from AuthenticationFilter
                        .pathMatchers(HttpMethod.DELETE, "/api/product/delete").hasRole("ADMIN") // Added from AuthenticationFilter

                        // --- All other exchanges require authentication ---
                        .anyExchange().permitAll()
                );

        return http.build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        // TODO: Restrict allowed origins in production!
        corsConfig.setAllowedOrigins(List.of("*")); // Use List.of for immutable list
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS","PATCH"));
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-User-Id", "X-User-Roles")); // Allow custom headers
        corsConfig.setExposedHeaders(Arrays.asList("Authorization")); // Expose headers if needed by frontend

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}