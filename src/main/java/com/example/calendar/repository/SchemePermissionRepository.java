package com.example.calendar.repository;

import com.example.calendar.model.SchemePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchemePermissionRepository extends JpaRepository<SchemePermission, Long> {
    boolean existsByPermissionKey(String permissionKey);
    Optional<SchemePermission> findByPermissionKey(String permissionKey);
}
