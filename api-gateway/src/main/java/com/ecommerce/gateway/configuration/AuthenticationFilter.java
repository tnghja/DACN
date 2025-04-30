package com.ecommerce.gateway.configuration;

import com.ecommerce.gateway.model.response.ApiResponse;
import com.ecommerce.gateway.model.response.IntrospectResponse;
import com.ecommerce.gateway.model.response.StatusEnum;
import com.ecommerce.gateway.service.IdentityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream; // Import Stream
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {

    IdentityService identityService;
    ObjectMapper objectMapper;
    AntPathMatcher pathMatcher = new AntPathMatcher();
    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    // Inject both lists, ensuring default value specifier ":" is present for both
    @Value("${app.security.public-paths:}") // Add default ":"
    @NonFinal
    private List<String> configuredPublicPaths;

    @Value("${app.security.user-paths:}") // Add default ":"
    @NonFinal
    private List<String> configuredUserPublicPaths;
    @Value("${app.security.search-paths:}") // Add default ":"
    @NonFinal
    private List<String> configuredSearchPaths;
    @Value("${app.security.product-paths:}") // Add default ":"
    @NonFinal
    private List<String> configuredProductPaths;
    @Value("${app.security.recombee-paths:}") // Add default ":"
    @NonFinal
    private List<String> configuredRecombeePaths;
    @Value("${app.security.password-paths:}") // Add default ":"
    @NonFinal
    private List<String> configuredPasswordPublicPaths;
    @Value("${app.swagger.paths}")
    @NonFinal
    private List<String> swaggerPaths;
    @Autowired
    public AuthenticationFilter(@Lazy IdentityService identityService, ObjectMapper objectMapper) {
        this.identityService = identityService;
        this.objectMapper = objectMapper;
    }


    // --- Modify isPublicEndpoint to check BOTH lists ---
    private boolean isPublicEndpoint(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        log.debug("Checking if path is public: {}", path);

        // Combine all configured public path lists into a single stream for checking
        // Use Stream.ofNullable to handle potentially null lists gracefully
        Stream<String> allPublicPathStream = Stream.of(
                        configuredProductPaths,
                        configuredPublicPaths,
                        configuredUserPublicPaths,
                        configuredSearchPaths,
                        configuredRecombeePaths,
                        configuredPasswordPublicPaths,
                        swaggerPaths
                )
                .filter(list -> list != null && !list.isEmpty())
                .flatMap(List::stream);

        // 2. **Collect the stream into a List BEFORE any terminal operation**
        List<String> allPatterns = allPublicPathStream.toList();



        // Check if the current request path matches any pattern in the combined stream
        boolean isPublic = allPatterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
// Use AntPathMatcher

        if (isPublic) {
            log.info("Path {} matches a configured public path pattern. Bypassing token check.", path);
        } else {
            log.debug("Path {} does not match any public path pattern. Proceeding with token check.", path);
        }
        return isPublic;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info(request.getURI().toString());

        if (isPublicEndpoint(request)) {
            log.info(String.valueOf(true));
            return chain.filter(exchange);
        }

        // ... (rest of filter logic is unchanged) ...
        List<String> authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader) || authHeader.getFirst() == null || !authHeader.getFirst().startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header for path: {}", request.getURI().getPath());
            return unAuthenticated(exchange.getResponse());
        }

        String token = authHeader.getFirst().substring(7);

        return identityService.introspect(token)
                .flatMap(apiResponse -> {
                    if (apiResponse.getStatus() == StatusEnum.SUCCESS &&
                            apiResponse.getPayload() != null &&
                            apiResponse.getPayload().isValid()) {

                        IntrospectResponse payload = apiResponse.getPayload();
                        List<String> roles = payload.getRoles() != null ? payload.getRoles() : Collections.emptyList();
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                payload.getUsername(), null, authorities);

                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header("X-User-Id", payload.getUserId())
                                .header("X-User-Roles", String.join(",", roles))
                                .build();
                        ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

                        log.debug("Token validated successfully for user: {}. Roles: {}. Path: {}", payload.getUsername(), roles, request.getURI().getPath());
                        return chain.filter(modifiedExchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                    } else {
                        log.warn("Token validation failed for path: {}. Reason: {}", request.getURI().getPath(), apiResponse.getError() != null ? apiResponse.getError() : "Invalid token");
                        return unAuthenticated(exchange.getResponse());
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("Error during token introspection for path: {}", request.getURI().getPath(), throwable);
                    return unAuthenticated(exchange.getResponse());
                });
    }

    @Override
    public int getOrder() {
        return -1; // Run before SecurityWebFilterChain
    }

    private boolean pathMatches(String path, String pattern) {
        // Basic matching logic (same as before)
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }
        if (pattern.endsWith("/**")) {
            String prefix = pattern.substring(0, pattern.length() - 3);
            // Ensure prefix comparison handles trailing slashes if necessary, or exact match if prefix is empty
            if (prefix.isEmpty()) return true; // "/**" matches everything
            return path.startsWith(prefix);
        }
        // Handle simple wildcard matching if needed more robustly, this is basic:
        if (pattern.contains("*") && !pattern.endsWith("/**")) {
            // This regex logic might need refinement depending on exact wildcard needs
            String regex = pattern.replace(".", "\\.").replace("*", "[^/]+");
            return path.matches(regex);
        }
        return path.equals(pattern);
    }

    private Mono<Void> unAuthenticated(ServerHttpResponse response) {
        // Error handling logic (same as before)
        return writeErrorResponse(response, HttpStatus.UNAUTHORIZED, Collections.singletonMap("error", "Unauthenticated - Invalid or missing token"));
    }

    private Mono<Void> writeErrorResponse(ServerHttpResponse response, HttpStatus status, Map<String, String> errorPayload) {
        // Error handling logic (same as before)
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(StatusEnum.ERROR);
        apiResponse.setError(errorPayload);

        try {
            byte[] responseBody = objectMapper.writeValueAsBytes(apiResponse);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(responseBody)));
        } catch (Exception e) {
            log.error("Error writing error response", e);
            // Ensure response completes even on error writing body
            if (!response.isCommitted()) {
                response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return response.setComplete();

        }
    }

} // End of class