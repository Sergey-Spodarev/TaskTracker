package com.example.calendar.repository;

import com.example.calendar.model.Task;
import com.example.calendar.model.TaskComment;
import com.example.calendar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    Optional<TaskComment> findByTaskAndUser(Task task, User user);
    boolean existsByTaskAndUser(Task task, User user);
}
