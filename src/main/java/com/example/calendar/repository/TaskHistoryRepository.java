package com.example.calendar.repository;

import com.example.calendar.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findByTask(Task task);
    List<TaskHistory> findByTaskOrderByChangedAtDesc(Task task);

    @Query("SELECT th FROM TaskHistory th WHERE th.task = :task ORDER BY th.changedAt DESC")
    List<TaskHistory> findTopNByTaskOrderByChangedAtDesc(@Param("task") Task task, @Param("limit") int limit);

    List<TaskHistory> findByChangedAtBefore(LocalDateTime date);

    // Для поиска по пользователю
    List<TaskHistory> findByChangedBy(User user);
    List<TaskHistory> findByChangedByAndChangedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    // Для поиска истории по проекту
    @Query("SELECT th FROM TaskHistory th WHERE th.task.project = :project")
    List<TaskHistory> findByProject(@Param("project") Project project);

    // Для поиска истории по отделу
    @Query("SELECT th FROM TaskHistory th WHERE th.task.project.department = :department")
    List<TaskHistory> findByDepartment(@Param("department") Department department);
}
