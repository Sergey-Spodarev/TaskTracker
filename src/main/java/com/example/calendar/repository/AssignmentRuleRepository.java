package com.example.calendar.repository;

import com.example.calendar.model.AssignmentRule;
import com.example.calendar.model.Company;
import com.example.calendar.model.Department;
import com.example.calendar.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRuleRepository extends JpaRepository<AssignmentRule, Long> {
    boolean existsById(Long id);
    List<AssignmentRule> findByRole(Role role);
    List<AssignmentRule> findByRole_Company(Company company);
    boolean existsByRoleAndSourceDepartmentAndTargetDepartmentAndAllowedTrue(
            Role role, Department source, Department target
    );
}
