package com.ecommerce.gateway.repository;

import com.ecommerce.gateway.configuration.CustomFeignConfig;
import com.ecommerce.gateway.model.response.ApiResponse;
import com.ecommerce.gateway.model.response.IntrospectResponse;
import com.ecommerce.gateway.model.request.IntrospectRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;


@FeignClient(name = "identity-service", url = "${app.services.identity}", configuration = CustomFeignConfig.class)
public interface IdentityClient {
    @RequestMapping(method = RequestMethod.POST,
            value = "/identity/auth/introspect",
            consumes = "application/json",
            produces = "application/json")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request);
}
