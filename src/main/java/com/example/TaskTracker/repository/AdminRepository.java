package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Tasks, Long> {

}
