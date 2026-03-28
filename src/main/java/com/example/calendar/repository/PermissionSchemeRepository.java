package com.example.calendar.repository;

import com.example.calendar.model.Company;
import com.example.calendar.model.PermissionScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PermissionSchemeRepository extends JpaRepository<PermissionScheme, Long> {
    List<PermissionScheme> findByCompany(Company company);
    Optional<PermissionScheme> findByCompanyAndId(Company company, Long id);
    @Query("SELECT ps FROM PermissionScheme ps " +
            "LEFT JOIN FETCH ps.systemPermissionDefinition spd " +
            "LEFT JOIN FETCH spd.systemPermissionCatalog " +
            "LEFT JOIN FETCH ps.customPermission cp " +
            "LEFT JOIN FETCH cp.schemePermission " +
            "WHERE ps.id = :id")
    Optional<PermissionScheme> findByIdWithPermissions(@Param("id") Long id);

    @Query("SELECT ps FROM PermissionScheme ps " +
            "LEFT JOIN FETCH ps.systemPermissionDefinition spd " +
            "LEFT JOIN FETCH spd.systemPermissionCatalog " +
            "LEFT JOIN FETCH ps.customPermission cp " +
            "LEFT JOIN FETCH cp.schemePermission " +
            "WHERE ps.company = :company")
    List<PermissionScheme> findByCompanyWithPermissions(@Param("company") Company company);
}
