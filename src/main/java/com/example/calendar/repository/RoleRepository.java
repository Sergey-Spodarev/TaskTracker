package com.example.calendar.repository;

import com.example.calendar.model.Company;
import com.example.calendar.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<List<Role>> findByCompany(Company company);
}
