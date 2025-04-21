package com.ecommerce.inventory.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Inventory {

    @Id
    private String productId; // Using product ID as the primary key for a 1:1 mapping feel

    @OneToOne(fetch = FetchType.LAZY) // Optional: Link back to Product if needed often
    @MapsId // Use the Product's ID as this entity's ID
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int availableQuantity; // Stock that can be sold/reserved now

    @Column(nullable = false)
    private int reservedQuantity;  // Stock held for pending/unconfirmed orders

    // Optional: Can be calculated (available + reserved) or stored
    // @Column(nullable = false)
    // private int totalStock;

    @Version // For optimistic locking to prevent race conditions
    private Long version;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime lastUpdatedAt;

    public void reserveStock(int quantity) {
        this.reservedQuantity += quantity;
    }

    public void confirmSale (int quantity) {
        this.reservedQuantity -= quantity;
    }

    public void releaseReservation(int quantity) {
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }
}