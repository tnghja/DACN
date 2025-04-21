package com.ecommerce.order.model.event;

public record InventoryItemRequest(String productId, int quantity) {}