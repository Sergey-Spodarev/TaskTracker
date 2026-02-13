package com.example.calendar.service;

import com.example.calendar.DTO.TaskHistoryDTO;
import com.example.calendar.model.SystemRole;
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
    private final SchemePermissionService schemePermissionService;

    public TaskHistoryService(TaskHistoryRepository taskHistoryRepository,
                              TaskRepository taskRepository,
                              SchemePermissionService schemePermissionService) {
        this.taskHistoryRepository = taskHistoryRepository;
        this.taskRepository = taskRepository;
        this.schemePermissionService = schemePermissionService;
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

        // Проверка прав через систему разрешений
        if (!schemePermissionService.hasPermission(currentUser, "view_task_history")) {
            throw new SecurityException("Недостаточно прав для просмотра истории задач");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        // Проверка принадлежности компании через цепочку: Task → Project → Department → Company
        if (!isUserInSameCompany(currentUser, task)) {
            throw new SecurityException("Доступ запрещён - задача не принадлежит вашей компании");
        }

        // Проверка дополнительных прав
        if (!hasAccessToTaskHistory(currentUser, task)) {
            throw new SecurityException("Нет прав на просмотр истории задачи");
        }

        List<TaskHistory> histories = taskHistoryRepository.findByTaskOrderByChangedAtDesc(task);
        return histories.stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Вспомогательный метод для проверки компании
    private boolean isUserInSameCompany(User user, Task task) {
        if (user.getCompany() == null ||
                task.getProject() == null ||
                task.getProject().getDepartment() == null ||
                task.getProject().getDepartment().getCompany() == null) {
            return false;
        }

        return user.getCompany().getId()
                .equals(task.getProject().getDepartment().getCompany().getId());
    }

    // Вспомогательный метод для проверки прав доступа к истории
    private boolean hasAccessToTaskHistory(User user, Task task) {
        // 1. Назначенный исполнитель
        boolean isAssignee = task.getAssignee() != null &&
                task.getAssignee().getId().equals(user.getId());

        // 2. Создатель задачи
        boolean isReporter = task.getReporter() != null &&
                task.getReporter().getId().equals(user.getId());

        // 3. Владелец компании
        boolean isCompanyOwner = user.getSystemRole() == SystemRole.COMPANY_OWNER;

        // 4. Руководитель отдела (если задача в его отделе)
        boolean isDepartmentHead = false;
        if (user.getSystemRole() == SystemRole.DEPARTMENT_HEAD &&
                user.getDepartment() != null &&
                task.getProject() != null &&
                task.getProject().getDepartment() != null) {
            isDepartmentHead = user.getDepartment().getId()
                    .equals(task.getProject().getDepartment().getId());
        }

        // 5. Менеджер проекта (если есть такая роль в системе)
        boolean isProjectManager = false;
        if (user.getRole() != null && "PROJECT_MANAGER".equals(user.getRole().getCode())) {
            isProjectManager = true;
        }

        return isAssignee || isReporter || isCompanyOwner ||
                isDepartmentHead || isProjectManager;
    }

    // Метод для получения последних записей истории
    public List<TaskHistoryDTO> getRecentTaskHistory(Long taskId, int limit) {
        User currentUser = getCurrentUser();

        if (!schemePermissionService.hasPermission(currentUser, "view_task_history")) {
            throw new SecurityException("Недостаточно прав для просмотра истории задач");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));

        // Проверка компании
        if (!isUserInSameCompany(currentUser, task)) {
            throw new SecurityException("Задача не принадлежит вашей компании");
        }

        // Простая проверка доступа для последних записей
        if (!hasAccessToTaskHistory(currentUser, task)) {
            throw new SecurityException("Нет прав на просмотр истории задачи");
        }

        List<TaskHistory> histories = taskHistoryRepository.findTopNByTaskOrderByChangedAtDesc(task, limit);
        return histories.stream()
                .map(this::convertToDTO)
                .toList();
    }

    // Метод для получения истории изменений по пользователю
    public List<TaskHistoryDTO> getUserActivityHistory(User user, LocalDateTime startDate, LocalDateTime endDate) {
        User currentUser = getCurrentUser();

        if (!schemePermissionService.hasPermission(currentUser, "view_user_activity")) {
            throw new SecurityException("Недостаточно прав для просмотра активности пользователей");
        }

        // Проверяем, что запрашиваемый пользователь в той же компании
        if (!currentUser.getCompany().getId().equals(user.getCompany().getId())) {
            throw new SecurityException("Нельзя просматривать активность пользователей из другой компании");
        }

        List<TaskHistory> histories = taskHistoryRepository
                .findByChangedByAndChangedAtBetween(user, startDate, endDate);

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
        taskHistoryDTO.setId(taskHistory.getId());
        taskHistoryDTO.setFieldName(taskHistory.getFieldName());
        taskHistoryDTO.setOldValue(taskHistory.getOldValue());
        taskHistoryDTO.setNewValue(taskHistory.getNewValue());
        taskHistoryDTO.setChangedAt(taskHistory.getChangedAt());

        if (taskHistory.getChangedBy() != null) {
            taskHistoryDTO.setChangedByName(taskHistory.getChangedBy().getUserName());
            taskHistoryDTO.setChangedById(taskHistory.getChangedBy().getId());
        }

        // Добавляем информацию о задаче
        if (taskHistory.getTask() != null) {
            taskHistoryDTO.setTaskId(taskHistory.getTask().getId());
            taskHistoryDTO.setTaskTitle(taskHistory.getTask().getTitle());

            // Добавляем информацию о проекте
            if (taskHistory.getTask().getProject() != null) {
                taskHistoryDTO.setProjectId(taskHistory.getTask().getProject().getId());
                taskHistoryDTO.setProjectName(taskHistory.getTask().getProject().getName());

                // Добавляем информацию об отделе
                if (taskHistory.getTask().getProject().getDepartment() != null) {
                    taskHistoryDTO.setDepartmentId(taskHistory.getTask().getProject().getDepartment().getId());
                    taskHistoryDTO.setDepartmentName(taskHistory.getTask().getProject().getDepartment().getName());
                }
            }
        }

        return taskHistoryDTO;
    }
}