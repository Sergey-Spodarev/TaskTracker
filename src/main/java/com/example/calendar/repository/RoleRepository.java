package com.example.calendar.repository;

import com.example.calendar.model.Company;
import com.example.calendar.model.Department;
import com.example.calendar.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Ищем роли по компании через department
    List<Role> findByDepartment_Company(Company company);

    // Проверяем существование по коду и компании (через department)
    boolean existsByCodeAndDepartment_Company(String code, Company company);

    // Ищем роль по коду и компании (через department)
    Optional<Role> findByCodeAndDepartment_Company(String code, Company company);

    Optional<Role> findByCodeAndDepartment(String code, Department department);
}
