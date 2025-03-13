package com.ecommerce.user.repository;


import com.ecommerce.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByUserName(String userName);
    Optional<User> findByUserName(String userName);
    Optional<User> findByUserId(String userId);
}
