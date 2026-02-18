package com.example.calendar.repository;

import com.example.calendar.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);

    @Query("SELECT t FROM Task t WHERE t.project.department = :department")
    List<Task> findByDepartment(@Param("department") Department department);

    List<Task> findByAssignee(User user);
    List<Task> findByReporter(User user);

    @Query("SELECT t FROM Task t WHERE t.project.department.company = :company")
    List<Task> findByProject_Department_Company(@Param("company") Company company);

    @Query("SELECT t FROM Task t WHERE t.assignee = :user OR t.reporter = :user")
    List<Task> findByAssigneeOrReporter(@Param("user") User user);

    Optional<Task> findById(Long id);
    boolean existsByAssigneeAndId(User user, Long id);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.subtasks WHERE t.id = :id")
    Optional<Task> findByIdWithSubtasks(@Param("id") Long id);
}
