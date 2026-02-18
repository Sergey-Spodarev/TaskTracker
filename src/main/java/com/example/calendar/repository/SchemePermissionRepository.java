package com.example.calendar.repository;

import com.example.calendar.model.PermissionScheme;
import com.example.calendar.model.SchemePermission;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchemePermissionRepository extends CrudRepository<SchemePermission, Long> {
    List<SchemePermission> findByScheme(PermissionScheme scheme);

    Optional<SchemePermission> findByIdAndScheme(Long id, PermissionScheme scheme);

    boolean existsByPermissionKey(String permissionKey);

    Optional<SchemePermission> findBySchemeAndPermissionKey(PermissionScheme scheme, String permissionKey);
}
