package com.example.calendar.repository;

import com.example.calendar.model.Department;
import com.example.calendar.model.RoleLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepartmentLevelRepository extends JpaRepository<RoleLevel, Integer> {
    boolean existsByDepartmentAndLevel(Department department, Integer level);
    Optional<RoleLevel> findByDepartmentAndLevel(Department department, Integer level);
}
