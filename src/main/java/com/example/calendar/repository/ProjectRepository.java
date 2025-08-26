package com.example.calendar.repository;

import com.example.calendar.model.Company;
import com.example.calendar.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByProjectKeyAndCompany(String projectName, Company company);
    Optional<Project> findByIdAndCompany(Long projectId, Company company);

    boolean existsByProjectKeyAndCompany(String projectKey, Company company);

    Optional<List<Project>> findAllByCompany(Company company);
}
