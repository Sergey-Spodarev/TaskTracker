package com.example.calendar.repository;

import com.example.calendar.model.PermissionScheme;
import com.example.calendar.model.SchemePermission;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SchemePermissionRepository extends CrudRepository<SchemePermission, Long> {
    List<SchemePermission> findByPermissionScheme(PermissionScheme permissionScheme);
    Optional<SchemePermission> findByIdAndPermissionScheme(Long id, PermissionScheme permissionScheme);
    boolean existsByPermissionKey(String permissionKey);
    Optional<SchemePermission> findBySchemeAndPermissionKey(PermissionScheme permissionScheme, String permissionKey);
}
