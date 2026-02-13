package com.example.calendar.repository;

import com.example.calendar.model.AssignmentDepartment;
import com.example.calendar.model.Company;
import com.example.calendar.model.Department;
import com.example.calendar.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentDepartmentRepository extends JpaRepository<AssignmentDepartment, Long> {
    boolean existsById(Long id);
    List<AssignmentDepartment> findByRole(Role role);
    List<AssignmentDepartment> findByRole_Company(Company company);
    boolean existsByRoleAndSourceDepartmentAndTargetDepartmentAndAllowedTrue(
            Role role, Department source, Department target
    );
}
