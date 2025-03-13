package com.ecommerce.product.config;

import com.recombee.api_client.RecombeeClient;
import com.recombee.api_client.util.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecombeeConfig {

    @Value("${recombee.database}")
    private String databaseId;

    @Value("${recombee.api_key}")
    private String apiKey;

    @Bean
    public RecombeeClient recombeeClient() {

        return new RecombeeClient(databaseId, apiKey).setRegion(Region.AP_SE);
    }
}
