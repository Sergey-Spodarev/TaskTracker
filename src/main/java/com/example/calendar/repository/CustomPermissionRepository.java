package com.example.calendar.repository;

import com.example.calendar.model.CustomPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomPermissionRepository extends JpaRepository<CustomPermission, Long> {
    Optional<CustomPermission> findByCode(String code);
    boolean existsByCode(String code);
}
