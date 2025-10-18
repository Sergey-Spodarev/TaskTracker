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

import java.util.List;

@Service
@Transactional
public class TaskCommentService {
    private final TaskCommentRepository taskCommentRepository;
    private final TaskService taskService;
    public TaskCommentService(TaskCommentRepository taskCommentRepository, TaskService taskService) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskService = taskService;
    }

    public TaskCommentDTO createTaskComment(TaskCommentDTO taskCommentDTO, Long taskId) {
        User user = getCurrentUser();

        Task task = taskService.GetTaskById(taskId);

        if (!task.getProject().getCompany().getId().equals(user.getCompany().getId())) {
            throw new AccessDeniedException("Доступ к задаче запрещён");
        }
        if (!task.getAssignee().getId().equals(user.getId()) && !task.getReporter().getId().equals(user.getId())) {
            throw new AccessDeniedException("Только исполнитель или создатель может оставить комментарий");
        }//надо потом добавить чтобы автоматом добавлялось в историю изменений и может добавить поле на проверку изменения и чтобы было то в визуале писало редактированно

        TaskComment taskComment = new TaskComment();
        taskComment.setTask(task);
        taskComment.setAuthor(user);
        taskComment.setComment(taskCommentDTO.getComment());
        return convertToDTO(taskCommentRepository.save(taskComment));
    }

    public TaskCommentDTO updateTaskComment(TaskCommentDTO taskCommentDTO, Long taskId, Long commentId) {//переделать надо чтобы ещё id передовала задачи
        User user = getCurrentUser();

        if (!taskService.existsByAssigneeAndId(user, taskId)) {
            throw new RuntimeException("Вы не можете сделать комментарий к данной задаче");
        }
        TaskComment taskComment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));

        if (!taskComment.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Комментарий не принадлежит указанной задаче");
        }
        if (!taskComment.getAuthor().equals(user)) {
            throw new AccessDeniedException("Вы можете редактировать только свои комментарии");
        }

        taskComment.setComment(taskCommentDTO.getComment());
        taskComment.setAuthor(user);
        return convertToDTO(taskCommentRepository.save(taskComment));
    }

    public void deleteTaskComment(Long taskId, Long commentId) {
        User user = getCurrentUser();

        TaskComment taskComment = taskCommentRepository.findByIdAndAuthor(commentId, user)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий не найден"));

        if (!taskComment.getTask().getProject().getCompany().equals(user.getCompany())) {
            throw new AccessDeniedException("Доступ запрещён");
        }
        if (!taskComment.getTask().getId().equals(taskId)) {
            throw new IllegalArgumentException("Комментарий не принадлежит указанной задаче");
        }
        if (!taskComment.getAuthor().equals(user)) {
            throw  new RuntimeException("Удалять можно только свои комментарии");
        }

        taskCommentRepository.delete(taskComment);
    }

    public List<TaskCommentDTO> getTaskComment(Long taskId) {
        User user = getCurrentUser();
        Task task = taskService.GetTaskById(taskId);

        if (user.getCompany() == null || task.getProject().getCompany() == null ||
                !user.getCompany().getId().equals(task.getProject().getCompany().getId())) {
            throw new AccessDeniedException("Доступ запрещён");
        }

        boolean isAssignee = task.getAssignee() != null && task.getAssignee().getId().equals(user.getId());
        boolean isReporter = task.getReporter() != null && task.getReporter().getId().equals(user.getId());
        boolean isAdmin = "ADMIN".equals(user.getRole().getCode());
        boolean isManager = "MANAGER".equals(user.getRole().getCode());

        boolean canView = isAssignee || isReporter || isAdmin || isManager;

        if (!canView) {
            throw new AccessDeniedException("Нет прав на просмотр комментариев задачи");
        }

        List<TaskComment> taskComments = taskCommentRepository.findByTask(task);
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
        taskCommentDTO.setAuthorId(taskComment.getAuthor().getId());
        taskCommentDTO.setAuthorName(taskComment.getAuthor().getUserName());
        taskCommentDTO.setCreatedAt(taskComment.getCreatedAt());
        return taskCommentDTO;
    }
}
