package com.ecommerce.order.repository.httpClient;

import com.ecommerce.order.error_handler.ProductErrorDecoder;
import com.ecommerce.order.error_handler.UserErrorDecoder;
import com.ecommerce.order.model.dto.CustomerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Repository
@FeignClient(name = "user-service", url = "${app.services.user}",
        configuration = UserErrorDecoder.class)
public interface UserClient {
    @GetMapping("/user/{customerId}")
    Optional<CustomerDTO> getCustomer(@PathVariable String customerId);
}
