package com.ecommerce.identityservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.identityservice.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {}
