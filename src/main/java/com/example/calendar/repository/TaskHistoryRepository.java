package com.example.calendar.repository;

import com.example.calendar.model.Task;
import com.example.calendar.model.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findByTask(Task task);
}
