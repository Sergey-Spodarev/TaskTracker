package com.example.calendar.repository;

import com.example.calendar.model.SystemPermissionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SystemPermissionDefinitionRepository extends JpaRepository<SystemPermissionDefinition, Integer> {
    Optional<SystemPermissionDefinition> findByCode(String code);
    boolean existsByCode(String code);

    boolean existsBySystemPermissionCatalog(List<String> systemPermissionCatalog);
}
