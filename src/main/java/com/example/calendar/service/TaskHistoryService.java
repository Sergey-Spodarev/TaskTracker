package com.example.calendar.service;

import com.example.calendar.DTO.TaskHistoryDTO;
import com.example.calendar.model.Task;
import com.example.calendar.model.TaskHistory;
import com.example.calendar.model.User;
import com.example.calendar.repository.TaskHistoryRepository;
import com.example.calendar.repository.TaskRepository;
import com.example.calendar.security.CustomUserDetails;
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
        User admin = getCurrentUser();
        if (!admin.getRole().getCode().equals("ADMIN")) {
            throw new RuntimeException("Только администратор может посмотреть историю изменения");
        }//надо подумать кто должен видеть историю изменений
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Данной задачи не существует"));

        if (!task.getProject().getCompany().equals(admin.getCompany())) {
            throw new RuntimeException("Доступ запрещён");
        }
        List<TaskHistory> taskHistories = taskHistoryRepository.findByTask(task);
        return taskHistories.stream()
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
        return taskHistoryDTO;
    }
}
