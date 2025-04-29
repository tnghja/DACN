package com.ecommerce.gateway.configuration;

import com.ecommerce.gateway.repository.IdentityClient;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import feign.jackson.JacksonEncoder;
import feign.jackson.JacksonDecoder;
import java.util.Collections;

@Configuration
public class WebClientConfiguration {
    @Bean
    WebClient webClient() {
        return WebClient.builder()
                .baseUrl("${app.service.identity}")
                .build();
    }
    @Bean
    IdentityClient identityClient(WebClient webClient) {
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient)).build();
        return httpServiceProxyFactory.createClient(IdentityClient.class);

    }
    @Bean
    public HttpMessageConverters httpMessageConverters() {
        return new HttpMessageConverters(Collections.singletonList(
                new MappingJackson2HttpMessageConverter()
        ));
    }
    @Bean
    public Encoder feignEncoder() {
        return new SpringFormEncoder(new JacksonEncoder());
    }
    @Bean
    public Decoder feignDecoder() {
        return new JacksonDecoder();
    }
}
