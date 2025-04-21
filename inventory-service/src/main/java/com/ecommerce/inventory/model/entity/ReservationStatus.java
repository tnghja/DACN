package com.ecommerce.inventory.model.entity;

public enum ReservationStatus {
    PENDING,   // Reserved but not confirmed (e.g., order not paid)
    CONFIRMED, // Reservation is active, stock deducted from available
    RELEASED,  // Reservation cancelled, stock returned to available
    EXPIRED    // Reservation timed out
}
