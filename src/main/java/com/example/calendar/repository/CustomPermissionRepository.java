package com.example.calendar.repository;

import com.example.calendar.model.CustomPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomPermissionRepository extends JpaRepository<CustomPermission, Long> {
    Optional<CustomPermission> findByCode(String code);
    boolean existsByCode(String code);
    @Query("SELECT cp FROM CustomPermission cp " +
            "LEFT JOIN FETCH cp.schemePermission " +
            "WHERE cp.id = :id")
    Optional<CustomPermission> findByIdWithSchemePermissions(@Param("id") Long id);
}
