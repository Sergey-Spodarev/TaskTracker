package com.example.calendar.service;

import com.example.calendar.DTO.TaskCommentDTO;
import com.example.calendar.model.Task;
import com.example.calendar.model.TaskComment;
import com.example.calendar.model.User;
import com.example.calendar.repository.TaskCommentRepository;
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
public class TaskCommentService {
    private final TaskCommentRepository taskCommentRepository;
    private final TaskService taskService;

    public TaskCommentService(TaskCommentRepository taskCommentRepository,
                              TaskService taskService) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskService = taskService;
    }

    public TaskCommentDTO createTaskComment(TaskCommentDTO taskCommentDTO, Long taskId) {
        User user = getCurrentUser();
        Task task = taskService.GetTaskById(taskId);

        // Проверка компании
        if (!task.getProject().getDepartment().getCompany().getId()
                .equals(user.getCompany().getId())) {
            throw new SecurityException("Задача не принадлежит вашей компании");
        }

        // Проверяем, что пользователь - создатель или исполнитель задачи
        boolean isReporter = task.getReporter() != null &&
                task.getReporter().getId().equals(user.getId());
        boolean isAssignee = task.getAssignee() != null &&
                task.getAssignee().getId().equals(user.getId());

        if (!isReporter && !isAssignee) {
            throw new SecurityException("Только создатель или исполнитель задачи может оставить комментарий");
        }

        TaskComment taskComment = new TaskComment();
        taskComment.setTask(task);
        taskComment.setAuthor(user);
        taskComment.setComment(taskCommentDTO.getComment());

        return convertToDTO(taskCommentRepository.save(taskComment));
    }

    public TaskCommentDTO updateTaskComment(Long taskId, Long commentId, TaskCommentDTO taskCommentDTO) {
        User user = getCurrentUser();

        Task task = taskService.GetTaskById(taskId);

        // Проверка компании
        if (!task.getProject().getDepartment().getCompany().getId()
                .equals(user.getCompany().getId())) {
            throw new SecurityException("Задача не принадлежит вашей компании");
        }

        TaskComment taskComment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));

        if (!taskComment.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Комментарий не принадлежит указанной задаче");
        }

        // Только автор комментария может его редактировать
        if (!taskComment.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("Вы можете редактировать только свои комментарии");
        }

        taskComment.setComment(taskCommentDTO.getComment());
        taskComment.setUpdatedAt(LocalDateTime.now());

        return convertToDTO(taskCommentRepository.save(taskComment));
    }

    public void deleteTaskComment(Long taskId, Long commentId) {
        User user = getCurrentUser();

        TaskComment taskComment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));

        if (!taskComment.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Комментарий не принадлежит указанной задаче");
        }

        // Проверка компании
        if (!taskComment.getTask().getProject().getDepartment().getCompany()
                .getId().equals(user.getCompany().getId())) {
            throw new SecurityException("Комментарий не принадлежит вашей компании");
        }

        // Только автор комментария может его удалить
        if (!taskComment.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("Вы можете удалять только свои комментарии");
        }

        taskCommentRepository.delete(taskComment);
    }

    public List<TaskCommentDTO> getTaskComments(Long taskId) {
        User user = getCurrentUser();
        Task task = taskService.GetTaskById(taskId);

        // Проверка компании
        if (!task.getProject().getDepartment().getCompany().getId()
                .equals(user.getCompany().getId())) {
            throw new SecurityException("Задача не принадлежит вашей компании");
        }

        // Проверяем доступ к задаче (только создатель или исполнитель)
        boolean isReporter = task.getReporter() != null &&
                task.getReporter().getId().equals(user.getId());
        boolean isAssignee = task.getAssignee() != null &&
                task.getAssignee().getId().equals(user.getId());

        if (!isReporter && !isAssignee) {
            throw new SecurityException("Только создатель или исполнитель задачи может просматривать комментарии");
        }

        List<TaskComment> taskComments = taskCommentRepository.findByTaskOrderByCreatedAtDesc(task);
        return taskComments.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private TaskCommentDTO convertToDTO(TaskComment taskComment) {
        TaskCommentDTO taskCommentDTO = new TaskCommentDTO();
        taskCommentDTO.setId(taskComment.getId());
        taskCommentDTO.setComment(taskComment.getComment());
        taskCommentDTO.setCreatedAt(taskComment.getCreatedAt());

        if (taskComment.getAuthor() != null) {
            taskCommentDTO.setAuthorId(taskComment.getAuthor().getId());
            taskCommentDTO.setAuthorName(taskComment.getAuthor().getUserName());
        }

        return taskCommentDTO;
    }
}