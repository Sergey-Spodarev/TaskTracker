package com.example.calendar.repository;

import com.example.calendar.model.Company;
import com.example.calendar.model.PermissionScheme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionSchemeRepository extends JpaRepository<PermissionScheme, Long> {
    List<PermissionScheme> findByCompany(Company company);
    Optional<PermissionScheme> findByCompanyAndId(Company company, Long id);
}
