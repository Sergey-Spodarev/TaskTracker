package com.example.calendar.repository;

import com.example.calendar.model.Task;
import com.example.calendar.model.TaskComment;
import com.example.calendar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    List<TaskComment> findByTask(Task task);
    List<TaskComment> findByTaskOrderByCreatedAtDesc(Task task);
}
