package com.example.calendar.service;

import com.example.calendar.DTO.TaskDTO;
import com.example.calendar.model.*;
import com.example.calendar.repository.ProjectRepository;
import com.example.calendar.repository.TaskRepository;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AssignmentRuleService assignmentRuleService;
    private final TaskHistoryService taskHistoryService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, ProjectRepository projectRepository, AssignmentRuleService assignmentRuleService, TaskHistoryService taskHistoryService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.assignmentRuleService = assignmentRuleService;
        this.taskHistoryService = taskHistoryService;
    }

    public TaskDTO createTask(TaskDTO taskDTO) {
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndCompany(taskDTO.getProjectId(), user.getCompany())
                .orElseThrow(() -> new UsernameNotFoundException("Нельзя дать задачу не своей группе"));
        User assigneeUser = userRepository.findById(taskDTO.getAssigneeId())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        if (!assignmentRuleService.canUserAssignToUser(user, assigneeUser)) {
            throw new AccessDeniedException(
                    "У вас нет прав назначать задачи пользователям из отдела " + assigneeUser.getDepartment().getName()
            );
        }

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setStartTime(taskDTO.getStart());
        task.setEndTime(taskDTO.getEnd());
        task.setReporter(user);
        task.setProject(project);
        task.setAssignee(assigneeUser);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(taskDTO.getPriority());

        Task saveTask = taskRepository.save(task);
        taskHistoryService.logTaskChange(task, user, "Task", "Task created", "Status: TODO");
        return convertTaskToDTO(saveTask);
    }

    public TaskDTO createSubtask(Long taskId, TaskDTO parentTaskDTO) {
        User user = getCurrentUser();
        Task parentTask = taskRepository.findByIdWithSubtasks(taskId)
                .orElseThrow(() -> new UsernameNotFoundException("Данной задачи не существует"));

        if (!parentTask.getAssignee().getCompany().equals(user.getCompany())) {
            throw new RuntimeException("Только сотрудник компании может делать подзадачи");
        }
        if (parentTask.getStatus() == TaskStatus.DONE){
            throw new RuntimeException("Данная задача уже выполнена к ней нельзя сделать подзадачу");
        }
        if (!parentTask.getAssignee().equals(user) && !parentTask.getReporter().equals(user)) {
            throw new RuntimeException("Только исполнитель или создатель задачи может делать подзадачи");
        }

        User assignee = userRepository.findById(parentTaskDTO.getAssigneeId())
                .orElseThrow(() -> new EntityNotFoundException("Исполнитель не найден"));
        if (!assignee.getCompany().equals(user.getCompany())) {
            throw new RuntimeException("Исполнитель должен быть из той же компании что и задача");
        }
        if (!assignmentRuleService.canUserAssignToUser(user, assignee)) {
            throw new RuntimeException("Вы не можете назначить задачу на " + assignee.getUserName());
        }

        Task subtask = new Task();
        subtask.setTitle(parentTaskDTO.getTitle());
        subtask.setStartTime(parentTaskDTO.getStart());
        subtask.setEndTime(parentTaskDTO.getEnd());
        subtask.setProject(parentTask.getProject());
        subtask.setAssignee(assignee);
        subtask.setReporter(user);
        subtask.setStatus(TaskStatus.TODO);
        subtask.setParentTask(parentTask);

        Task saveSubtask = taskRepository.save(subtask);
        long count = parentTask.getSubtasks().size();
        if (count == 0) {
            taskHistoryService.logTaskChange(parentTask, user, "Subtask", "None", "Created");
        }
        else {
            taskHistoryService.logTaskChange(parentTask, user, "Subtask", count + " subtask", (count + 1) + " subtask");
        }

        return convertTaskToDTO(saveSubtask);
    }

    public TaskDTO changeAssignee(Long taskId, Long assigneeId) {
        if (assigneeId == null || assigneeId <= 0) {
            throw new IllegalArgumentException("Некорректный assigneeId");
        }
        User newUser = userRepository.findById(assigneeId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new UsernameNotFoundException("Данной задачи не существует"));

        if (!user.getCompany().getId().equals(task.getProject().getCompany().getId())) {
            throw new AccessDeniedException("Вы можете менять задачи только в своей компании");
        }
        if (!task.getProject().getCompany().getId().equals(newUser.getCompany().getId())) {
            throw new AccessDeniedException("Исполнитель должен быть из той же компании как и сама задача");
        }
        if (!assignmentRuleService.canUserAssignToUser(user, newUser)) {
            throw new AccessDeniedException("Вы не можете поставить задачу на " + newUser.getUserName());
        }

        taskHistoryService.logTaskChange(task, user, "Assignee", task.getAssignee().getUserName(), newUser.getUserName());
        task.setAssignee(newUser);
        return convertTaskToDTO(taskRepository.save(task));
    }

    public TaskDTO changeStatus(Long taskId, TaskStatus newStatus) {
        User user = getCurrentUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Данной задачи не существует"));

        if (!user.getCompany().getId().equals(task.getProject().getCompany().getId())) {
            throw new AccessDeniedException("Вы можете менять задачи только в своей компании");
        }
        if (!task.getAssignee().getUserId().equals(user.getUserId())){
            throw new AccessDeniedException("Только исполнитель может менять статус задачи");
        }

        if (newStatus == TaskStatus.DONE && !task.getSubtasks().isEmpty()) {
            boolean allDone = task.getSubtasks().stream()
                    .allMatch(subtask -> subtask.getStatus() == TaskStatus.DONE);
            if (!allDone) {
                throw new AccessDeniedException("Нельзя закрыть задачу: не все подзадачи выполнены");
            }
        }

        TaskStatus oldStatus = task.getStatus();
        if (!TaskStatus.canTransition(oldStatus, newStatus)){
            throw new IllegalStateException("Переход из " + task.getStatus() + " в " + newStatus + " запрещён");
        }

        task.setStatus(newStatus);
        taskHistoryService.logTaskChange(task, user, "Status", oldStatus.toString(), newStatus.toString());
        return convertTaskToDTO(taskRepository.save(task));
    }

    public TaskDTO changeEndTime(Long taskId, LocalDateTime endTime) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Данной задачи не существует"));

        if (!user.getCompany().getId().equals(task.getProject().getCompany().getId())) {
            throw new AccessDeniedException("Вы можете менять задачи только в своей компании");
        }
        if (!task.getReporter().getUserId().equals(user.getUserId())){
            throw new AccessDeniedException("Только тот кто поставил задачу может менять срок выполнения задачи");
        }
        if (!endTime.isAfter(task.getStartTime())) {
            throw new IllegalArgumentException("Время окончания должно быть позже начала");
        }

        taskHistoryService.logTaskChange(task, user, "EndTime", task.getEndTime().toString(), endTime.toString());
        task.setEndTime(endTime);
        return convertTaskToDTO(taskRepository.save(task));
    }

    public TaskDTO changeTitle(Long taskId, String title) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Данной задачи не существует"));

        if (!user.getCompany().getId().equals(task.getProject().getCompany().getId())) {
            throw new AccessDeniedException("Вы можете менять данные задачи только в своей компании");
        }
        if (!task.getReporter().getUserId().equals(user.getUserId())){
            throw new AccessDeniedException("Только тот кто поставил задачу может менять название задачи");
        }
        if (task.getTitle().equals(title)) {
            throw new IllegalArgumentException("Название задачи одинаково с предыдущему");
        }

        taskHistoryService.logTaskChange(task, user, "Title", task.getTitle(), title);
        task.setTitle(title);
        return convertTaskToDTO(taskRepository.save(task));
    }

    public TaskDTO changePriority(Long taskId, TaskPriority priority) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Данной задачи не существует"));

        if (!user.getCompany().getId().equals(task.getProject().getCompany().getId())) {
            throw new AccessDeniedException("Вы можете менять данные задачи только в своей компании");
        }
        if (!task.getReporter().getUserId().equals(user.getUserId())){
            throw new AccessDeniedException("Только тот кто поставил задачу может менять её приоритет");
        }
        if (task.getPriority().equals(priority)) {
            throw new IllegalArgumentException("Приоритет не изменился");
        }

        taskHistoryService.logTaskChange(task, user, "Priority", task.getPriority().toString(), priority.toString());
        task.setPriority(priority);
        return convertTaskToDTO(taskRepository.save(task));
    }

    public TaskDTO getTaskById(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Задача не найдена"));
        if (!user.getCompany().getId().equals(task.getProject().getCompany().getId())) {
            throw new AccessDeniedException("Вы не можете смотреть задачи другой компании");
        }
        return convertTaskToDTO(task);
    }

    public List<TaskDTO> getTasksByAssignee(){
        User user = getCurrentUser();
        return taskRepository.findByAssignee(user).stream()
                .map(this::convertTaskToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByReporter(){
        User user = getCurrentUser();
        return taskRepository.findByReporter(user).stream()
                .map(this::convertTaskToDTO)
                .toList();
    }

    public List<TaskDTO> getTasksByProjectId(Long projectId){
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndCompany(projectId, user.getCompany())
                .orElseThrow(() -> new UsernameNotFoundException("Данная задача не найдена внутри вашего проекта"));
        return taskRepository.findByProject(project).stream()
                .map(this::convertTaskToDTO)
                .toList();
    }

    public List<TaskDTO> getSubTasksByTaskId(Long taskId){
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Данной задачи не существует"));

        if (!user.getCompany().getId().equals(task.getProject().getCompany().getId())) {
            throw new AccessDeniedException("Вы не можете получить задачи другой компании");
        }//надо подумать все могут смотреть подзадачи или нет

        List<Task> subtasks = task.getSubtasks();
        return subtasks.stream()
                .map(this::convertTaskToDTO)
                .toList();
    }

    public List<TaskDTO> getAllTasksInCompany(){
        User user = getCurrentUser();
        Company company = user.getCompany();
        List<Task> task = taskRepository.findByProject_Company(company);
        return task.stream()
                .map(this::convertTaskToDTO)
                .toList();
    }

    public void deleteTask(Long taskId){
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Данной задачи не существует"));

        if (!user.getCompany().getId().equals(task.getProject().getCompany().getId())) {
            throw new AccessDeniedException("Вы не можете получить задачи другой компании");
        }
        if (!task.getReporter().getUserId().equals(user.getUserId())){
            throw new AccessDeniedException("Только создатель может удалить задачу");
        }

        taskHistoryService.logTaskChange(task, user, "Delete Task", task.getTitle(), task.getTitle());
        taskRepository.delete(task);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private TaskDTO convertTaskToDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(task.getId());
        taskDTO.setTitle(task.getTitle());
        taskDTO.setStart(task.getStartTime());
        taskDTO.setEnd(task.getEndTime());
        taskDTO.setStatus(task.getStatus());
        taskDTO.setPriority(task.getPriority());
        User assignee = task.getAssignee();
        if (assignee != null) {
            taskDTO.setAssigneeId(assignee.getUserId());
            taskDTO.setAssigneeName(assignee.getUserName());
        }
        return taskDTO;
    }
}
