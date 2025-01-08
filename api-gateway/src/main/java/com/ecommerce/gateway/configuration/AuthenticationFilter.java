package com.ecommerce.gateway.configuration;

import com.ecommerce.gateway.model.response.ApiResponse;
import com.ecommerce.gateway.model.response.StatusEnum;
import com.ecommerce.gateway.service.IdentityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {

    IdentityService identityService;
    ObjectMapper objectMapper;

    @NonFinal
    private String [] publicEndpoints = {"/identity/auth/.*", "identity/users/register"};

    @Value("${app.api-prefix}")
    @NonFinal
    private String apiPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        if(isPublicEndpoint(exchange.getRequest())){
            return  chain.filter(exchange);
        }

        List<String> authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (CollectionUtils.isEmpty(authHeader)){
            return unAuthenticated(exchange.getResponse());
        }


        String token = authHeader.getFirst().replace("Bearer", "");
        return identityService.introspect(token).flatMap(introspectResponse -> {
            if(introspectResponse.getStatus().equals(StatusEnum.ERROR)){
                return chain.filter(exchange);
            }
            else {
                return unAuthenticated(exchange.getResponse());
            }
        }).onErrorResume(throwable -> unAuthenticated(exchange.getResponse()));

    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean isPublicEndpoint(ServerHttpRequest request) {
        return Arrays.stream(publicEndpoints).anyMatch(each -> request.getURI().getPath().matches(apiPrefix + each));
    }

    public Mono<Void> unAuthenticated(ServerHttpResponse response) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.error(Collections.singletonMap("error", "Unauthenticated"));

        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try {
            byte[] responseBody = objectMapper.writeValueAsBytes(apiResponse);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(responseBody)));
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
    