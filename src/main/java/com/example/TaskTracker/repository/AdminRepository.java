package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Tasks, Long> {
    Optional<Tasks> findById(Long id);
}
