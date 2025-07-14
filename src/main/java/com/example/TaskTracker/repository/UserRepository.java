package com.example.TaskTracker.repository;

import com.example.TaskTracker.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);
    List<Users> findByRole(String role);
    List<Users> findAllByRole(String role);
    @Query("SELECT DISTINCT u FROM Users u " +
            "LEFT JOIN FETCH u.createTasks " +
            "LEFT JOIN FETCH u.assignedTasks " +
            "WHERE u.role = 'USER'")
    List<Users> findUsersWithTasks();
}
