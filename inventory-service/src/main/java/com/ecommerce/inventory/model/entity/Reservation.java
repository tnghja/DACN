package com.ecommerce.inventory.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservation_orderid", columnList = "orderId"),
        @Index(name = "idx_reservation_status_expiry", columnList = "status, expiresAt")
})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // UUID is good for distributed systems
    private UUID reservationId;

    @Column(nullable = false, unique = true) // Assuming one reservation per order initially
    private String orderId; // ID from the Order Service

    @Column(nullable = false)
    private String productId; // Which product is reserved

    @Column(nullable = false)
    private int quantity;   // How many units are reserved

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
