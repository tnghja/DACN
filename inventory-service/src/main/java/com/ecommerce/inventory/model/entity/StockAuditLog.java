package com.ecommerce.inventory.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_audit_log", indexes = {
        @Index(name = "idx_audit_productid_time", columnList = "productId, timestamp")
})
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StockAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(nullable = false)
    private String productId;

    @Column // Optional: Link to the order that caused the change
    private String orderId;

    @Column // Optional: Link to the reservation involved
    private UUID reservationId;

    @Enumerated(EnumType.STRING)
//    @Column(nullable = false, length = 30)
    private StockChangeType changeType;

//    @Column(nullable = false)
    private int quantityChange; // Positive for increase, negative for decrease

//    @Column(nullable = false)
    private int previousAvailableQuantity;

//    @Column(nullable = false)
    private int newAvailableQuantity;

//    @Column(nullable = false)
    private int previousReservedQuantity;

//    @Column(nullable = false)
    private int newReservedQuantity;

    @Column(length = 255)
    private String reason; // Optional: For manual adjustments
//
//    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime timestamp;


    public StockAuditLog(String productId, String orderId, Object o, StockChangeType stockChangeType, int i, int previousAvailable, int availableQuantity, int previousReserved, int reservedQuantity, String s) {
    }
}
