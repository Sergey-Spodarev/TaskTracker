package com.example.calendar.repository;

import com.example.calendar.model.AssignmentRule;
import com.example.calendar.model.Company;
import com.example.calendar.model.Role;
import org.hibernate.sql.ast.tree.update.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentRuleRepository extends JpaRepository<AssignmentRule, Long> {
    boolean existsById(Long id);

    Optional<List<AssignmentRule>> findByRole(Role role);
    Optional<List<AssignmentRule>> findByRole_Company(Company company);
}
