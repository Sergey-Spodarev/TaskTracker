package com.example.calendar.repository;

import com.example.calendar.model.Company;
import com.example.calendar.model.Project;
import com.example.calendar.model.Task;
import com.example.calendar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProject(Project project);
    List<Task> findByAssignee(User user);
    List<Task> findByReporter(User user);
    List<Task> findByProject_Company(Company company);
}
