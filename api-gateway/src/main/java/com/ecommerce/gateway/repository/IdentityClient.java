package com.ecommerce.gateway.repository;

import com.ecommerce.gateway.model.response.ApiResponse;
import com.ecommerce.gateway.model.response.IntrospectResponse;
import com.ecommerce.gateway.model.request.IntrospectRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import java.lang.management.MonitorInfo;

public interface IdentityClient {
    @PostExchange(url = "/auth/introspect", contentType = MediaType.APPLICATION_JSON_VALUE)
    Mono<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request);

}
