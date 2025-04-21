package com.ecommerce.inventory.model.entity;


public enum StockChangeType {
    INBOUND_RECEIPT,       // New stock received
    OUTBOUND_SALE,         // Stock shipped for a confirmed order
    RESERVATION_CREATED,   // Stock moved from available to reserved
    RESERVATION_CONFIRMED, // (Optional) Mark reservation as final, triggers outbound if needed
    RESERVATION_RELEASED,  // Stock moved from reserved back to available
    ADJUSTMENT_MANUAL,     // Manual correction
    ADJUSTMENT_DAMAGED,    // Stock written off as damaged
    ADJUSTMENT_LOST        // Stock written off as lost
}
