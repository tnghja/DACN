package com.ecommerce.search_service.configuration;

import io.pinecone.clients.Pinecone;

public class PineconeConnector {
    public static void main(String[] args) {
        Pinecone pc = new Pinecone.Builder("YOUR_API_KEY").build();
    }
}
