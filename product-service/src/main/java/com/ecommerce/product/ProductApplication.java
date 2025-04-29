package com.ecommerce.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.ControllerAdvice;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients(basePackages = "com.ecommerce.product.repository.httpClient")
public class ProductApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProductApplication.class, args);
	}

}
