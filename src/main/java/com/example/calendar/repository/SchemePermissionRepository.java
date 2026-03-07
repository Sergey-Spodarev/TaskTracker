package com.example.calendar.repository;

import com.example.calendar.model.SchemePermission;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchemePermissionRepository extends CrudRepository<SchemePermission, Long> {
    boolean existsByPermissionKey(String permissionKey);
    Optional<SchemePermission> findByPermissionKey(String permissionKey);
    List<SchemePermission> getAll();
}
