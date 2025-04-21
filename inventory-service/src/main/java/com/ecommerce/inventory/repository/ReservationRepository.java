package com.ecommerce.inventory.repository;

import com.ecommerce.inventory.model.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    boolean existsByOrderId(String orderId);

    List<Reservation> findByOrderId(String orderId);
}
