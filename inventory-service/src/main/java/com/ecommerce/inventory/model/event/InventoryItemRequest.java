package com.ecommerce.inventory.model.event;

public record InventoryItemRequest(String productId, int quantity) {}