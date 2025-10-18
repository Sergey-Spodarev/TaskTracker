package com.example.calendar.service;

import com.example.calendar.DTO.TaskHistoryDTO;
import com.example.calendar.model.Task;
import com.example.calendar.model.TaskHistory;
import com.example.calendar.model.User;
import com.example.calendar.repository.TaskHistoryRepository;
import com.example.calendar.repository.TaskRepository;
import com.example.calendar.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskHistoryService {
    private final TaskHistoryRepository taskHistoryRepository;
    private final TaskRepository taskRepository;
    public TaskHistoryService(TaskHistoryRepository taskHistoryRepository, TaskRepository taskRepository) {
        this.taskHistoryRepository = taskHistoryRepository;
        this.taskRepository = taskRepository;
    }

    public void logTaskChange(
            Task task,
            User changedBy,
            String fieldName,
            String oldValue,
            String newValue) {

        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setChangedBy(changedBy);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangedAt(LocalDateTime.now());

        taskHistoryRepository.save(history);
    }

    public List<TaskHistoryDTO> getAllTaskHistory(Long taskId) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        if (currentUser.getCompany() == null || task.getProject().getCompany() == null ||
                !currentUser.getCompany().getId().equals(task.getProject().getCompany().getId())) {
            throw new AccessDeniedException("Доступ запрещён");
        }

        boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(currentUser.getId());
        boolean isReporter = task.getReporter() != null && task.getReporter().getId().equals(currentUser.getId());
        boolean isAdmin = "ADMIN".equals(currentUser.getRole().getCode());
        boolean isManager = "MANAGER".equals(currentUser.getRole().getCode());

        boolean canView = isAssignee || isReporter || isAdmin || isManager;

        if (!canView) {
            throw new AccessDeniedException("Нет прав на просмотр истории задачи");
        }

        List<TaskHistory> histories = taskHistoryRepository.findByTask(task);
        return histories.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser();
    }

    private TaskHistoryDTO convertToDTO(TaskHistory taskHistory) {
        TaskHistoryDTO taskHistoryDTO = new TaskHistoryDTO();
        taskHistoryDTO.setFieldName(taskHistory.getFieldName());
        taskHistoryDTO.setOldValue(taskHistory.getOldValue());
        taskHistoryDTO.setNewValue(taskHistory.getNewValue());
        taskHistoryDTO.setChangedByName(taskHistory.getChangedBy().getUserName());
        taskHistoryDTO.setChangedById(taskHistory.getChangedBy().getId());
        taskHistoryDTO.setChangedAt(taskHistory.getChangedAt());
        return taskHistoryDTO;
    }
}
