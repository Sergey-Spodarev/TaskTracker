package com.example.calendar.repository;

import com.example.calendar.model.Company;
import com.example.calendar.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByIdAndCompany(Long projectId, Company company);

    boolean existsByProjectKeyAndCompany(String projectKey, Company company);

    List<Project> findByCompanyId(Long companyId);
}
