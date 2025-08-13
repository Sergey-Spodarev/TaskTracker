package com.example.calendar.repository;

import com.example.calendar.model.Company;
import com.example.calendar.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByCompanyAndName(Company company, String name);
    Optional<List<Department>> findByCompany(Company company);
}
