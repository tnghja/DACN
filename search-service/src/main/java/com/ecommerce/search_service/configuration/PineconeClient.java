package com.ecommerce.search_service.configuration;

import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PineconeClient {
    //    private final String PINECONE_API_KEY = "pcsk_KUtgS_DZYC6qjTGi8VoK2rEEX99z41P48ug7oX2gAM6XbJvMMqm8aRkyfmzEfHVXeRpLB";
//    private final String PINECONE_INDEX_NAME = "image-search";
    @Value("${pinecone.api-key}")
    private String PINECONE_API_KEY;

    @Value("${pinecone.index-name}")
    private String PINECONE_INDEX_NAME;

    @Bean
    public Index pineconeIndex() {
        Pinecone pineconeClient = new Pinecone.Builder(PINECONE_API_KEY).build();
        return pineconeClient.getIndexConnection(PINECONE_INDEX_NAME);
    }
}