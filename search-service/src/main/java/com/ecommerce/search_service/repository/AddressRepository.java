package com.ecommerce.search_service.repository;

import com.ecommerce.user.model.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}