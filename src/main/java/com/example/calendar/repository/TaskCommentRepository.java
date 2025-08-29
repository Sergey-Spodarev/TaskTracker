package com.example.calendar.repository;

import com.example.calendar.model.Task;
import com.example.calendar.model.TaskComment;
import com.example.calendar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    Optional<TaskComment> findByIdAndUser(Long id, User user);
    List<TaskComment> findByTask(Task task);
}
