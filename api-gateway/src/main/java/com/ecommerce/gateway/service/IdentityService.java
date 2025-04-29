package com.ecommerce.gateway.service;

import com.ecommerce.gateway.model.request.IntrospectRequest;
import com.ecommerce.gateway.model.response.ApiResponse;
import com.ecommerce.gateway.model.response.IntrospectResponse;
import com.ecommerce.gateway.repository.IdentityClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityService {

    private final IdentityClient identityClient;

    public IdentityService(IdentityClient identityClient) {
        this.identityClient = identityClient;
    }

    public Mono<ApiResponse<IntrospectResponse>> introspect(String token) {
        IntrospectRequest request = new IntrospectRequest();
        request.setToken(token);

        // Convert the synchronous Feign response to a Mono
        return Mono.fromCallable(() -> identityClient.introspect(request))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
