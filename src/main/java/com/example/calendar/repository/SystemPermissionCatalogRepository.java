package com.example.calendar.repository;

import com.example.calendar.model.SystemPermissionCatalog;
import com.example.calendar.model.SystemPermissionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemPermissionCatalogRepository extends JpaRepository<SystemPermissionCatalog, Long> {
    boolean existsByKey(SystemPermissionKey key);
    Optional<SystemPermissionCatalog> findByKey(SystemPermissionKey key);
    Optional<SystemPermissionCatalog> findByName(String name);
    List<SystemPermissionCatalog> getAllPermissionCatalog();
    List<SystemPermissionCatalog> findByGroup(String group);
    List<SystemPermissionCatalog> findAllByOrderByGroupAsc();
}
