package com.example.calendar.repository;

import com.example.calendar.model.Company;
import com.example.calendar.model.Role;
import com.example.calendar.model.RoleLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleLevelRepository extends JpaRepository<RoleLevel, Long> {

    @Query("SELECT rl FROM RoleLevel rl " +
            "JOIN rl.role r " +
            "JOIN r.department d " +
            "WHERE rl.role = :role AND d.company = :company")
    Optional<RoleLevel> findByRoleAndCompany(@Param("role") Role role, @Param("company") Company company);

    @Query("SELECT rl FROM RoleLevel rl " +
            "JOIN rl.role r " +
            "JOIN r.department d " +
            "WHERE d.company = :company")
    List<RoleLevel> findAllByCompany(@Param("company") Company company);
}
