package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.model.entity.StockAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAuditLogRepository extends JpaRepository<StockAuditLog, Integer> {
}
