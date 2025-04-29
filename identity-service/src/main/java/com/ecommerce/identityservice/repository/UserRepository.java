package com.ecommerce.identityservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.identityservice.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String username);
    Optional<User> findByUserId(String userId);
    Optional<User> findByEmail(String username);
}
