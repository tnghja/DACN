package com.ecommerce.order.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OnlinePayment extends UserPayment {
    private String transaction_id;

    @Column(insertable = false, updatable = false) // Prevent duplication in mapping
    private String payment_id;
}

