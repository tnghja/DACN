package com.ecommerce.identityservice.repository.httpClient;


import com.ecommerce.identityservice.dto.request.UserProfileCreateRequest;
import com.ecommerce.identityservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Repository
@FeignClient(name = "user-service", url = "${app.services.user}")
public interface UserClient {
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    UserResponse createUserProfile(@RequestBody UserProfileCreateRequest userProfileCreateRequest);

}


