package com.example.calendar.service;

import com.example.calendar.DTO.TaskDTO;
import com.example.calendar.model.*;
import com.example.calendar.repository.DepartmentRepository;
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
    private final SchemePermissionService schemePermissionService;
    private final DepartmentRepository departmentRepository;

    public TaskService(TaskRepository taskRepository,
                       UserRepository userRepository,
                       ProjectRepository projectRepository,
                       AssignmentRuleService assignmentRuleService,
                       TaskHistoryService taskHistoryService,
                       SchemePermissionService schemePermissionService,
                       DepartmentRepository departmentRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.assignmentRuleService = assignmentRuleService;
        this.taskHistoryService = taskHistoryService;
        this.schemePermissionService = schemePermissionService;
        this.departmentRepository = departmentRepository;
    }

    public TaskDTO createTask(TaskDTO taskDTO) {
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "create_task")) {
            throw new SecurityException("Недостаточно прав для создания задачи");
        }

        Project project = projectRepository.findByIdAndDepartment_Company(
                        taskDTO.getProjectId(), user.getCompany())
                .orElseThrow(() -> new RuntimeException("Проект не найден в вашей компании"));

        User assigneeUser = userRepository.findById(taskDTO.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!assigneeUser.getCompany().getId().equals(user.getCompany().getId())) {
            throw new SecurityException("Исполнитель должен быть из вашей компании");
        }

        if (!assignmentRuleService.canUserAssignToUser(user, assigneeUser)) {
            throw new SecurityException(
                    "У вас нет прав назначать задачи пользователям из отдела " +
                            assigneeUser.getDepartment().getName()
            );
        }

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
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

        if (!schemePermissionService.hasPermission(user, "create_subtask")) {
            throw new SecurityException("Недостаточно прав для создания подзадачи");
        }

        Task parentTask = taskRepository.findByIdWithSubtasks(taskId)
                .orElseThrow(() -> new RuntimeException("Данной задачи не существует"));

        if (!isUserInSameCompany(user, parentTask)) {
            throw new SecurityException("Только сотрудник компании может делать подзадачи");
        }

        if (parentTask.getStatus() == TaskStatus.DONE){
            throw new RuntimeException("Данная задача уже выполнена, к ней нельзя добавить подзадачу");
        }

        boolean canCreateSubtask = parentTask.getAssignee() != null &&
                parentTask.getAssignee().getId().equals(user.getId()) ||
                parentTask.getReporter() != null &&
                        parentTask.getReporter().getId().equals(user.getId());

        if (!canCreateSubtask) {
            throw new SecurityException("Только исполнитель или создатель задачи может создавать подзадачи");
        }

        User assignee = userRepository.findById(parentTaskDTO.getAssigneeId())
                .orElseThrow(() -> new RuntimeException("Исполнитель не найден"));

        if (!assignee.getCompany().getId().equals(user.getCompany().getId())) {
            throw new SecurityException("Исполнитель должен быть из той же компании");
        }

        if (!assignmentRuleService.canUserAssignToUser(user, assignee)) {
            throw new SecurityException("Вы не можете назначить задачу на " + assignee.getUserName());
        }

        Task subtask = new Task();
        subtask.setTitle(parentTaskDTO.getTitle());
        subtask.setDescription(parentTaskDTO.getDescription());
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
        } else {
            taskHistoryService.logTaskChange(parentTask, user, "Subtask",
                    count + " subtask", (count + 1) + " subtask");
        }

        return convertTaskToDTO(saveSubtask);
    }

    public TaskDTO changeAssignee(Long taskId, Long assigneeId) {
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "change_assignee")) {
            throw new SecurityException("Недостаточно прав для смены исполнителя");
        }

        User newUser = userRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Данной задачи не существует"));

        if (!isUserInSameCompany(user, task)) {
            throw new SecurityException("Вы можете менять задачи только в своей компании");
        }

        if (!newUser.getCompany().getId().equals(user.getCompany().getId())) {
            throw new SecurityException("Исполнитель должен быть из той же компании");
        }

        if (!assignmentRuleService.canUserAssignToUser(user, newUser)) {
            throw new SecurityException("Вы не можете назначить задачу на " + newUser.getUserName());
        }

        String oldAssignee = task.getAssignee() != null ? task.getAssignee().getUserName() : "None";
        taskHistoryService.logTaskChange(task, user, "Assignee", oldAssignee, newUser.getUserName());
        task.setAssignee(newUser);

        return convertTaskToDTO(taskRepository.save(task));
    }

    public TaskDTO changeStatus(Long taskId, TaskStatus newStatus) {
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "change_task_status")) {
            throw new SecurityException("Недостаточно прав для смены статуса задачи");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Данной задачи не существует"));

        if (!isUserInSameCompany(user, task)) {
            throw new SecurityException("Вы можете менять задачи только в своей компании");
        }

        if (task.getAssignee() == null || !task.getAssignee().getId().equals(user.getId())) {
            throw new SecurityException("Только исполнитель может менять статус задачи");
        }

        if (newStatus == TaskStatus.DONE && !task.getSubtasks().isEmpty()) {
            boolean allDone = task.getSubtasks().stream()
                    .allMatch(subtask -> subtask.getStatus() == TaskStatus.DONE);
            if (!allDone) {
                throw new SecurityException("Нельзя закрыть задачу: не все подзадачи выполнены");
            }
        }

        TaskStatus oldStatus = task.getStatus();
        if (!TaskStatus.canTransition(oldStatus, newStatus)){
            throw new IllegalStateException("Переход из " + oldStatus + " в " + newStatus + " запрещён");
        }

        task.setStatus(newStatus);
        taskHistoryService.logTaskChange(task, user, "Status", oldStatus.toString(), newStatus.toString());

        return convertTaskToDTO(taskRepository.save(task));
    }

    public TaskDTO changeEndTime(Long taskId, LocalDateTime endTime) {
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "change_task_deadline")) {
            throw new SecurityException("Недостаточно прав для смены дедлайна задачи");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Данной задачи не существует"));

        if (!isUserInSameCompany(user, task)) {
            throw new SecurityException("Вы можете менять задачи только в своей компании");
        }

        if (!task.getReporter().getId().equals(user.getId())){
            throw new SecurityException("Только тот кто поставил задачу может менять срок выполнения задачи");
        }

        if (!endTime.isAfter(task.getStartTime())) {
            throw new IllegalArgumentException("Время окончания должно быть позже начала");
        }

        taskHistoryService.logTaskChange(task, user, "EndTime",
                task.getEndTime().toString(), endTime.toString());
        task.setEndTime(endTime);

        return convertTaskToDTO(taskRepository.save(task));
    }

    public TaskDTO changeTitle(Long taskId, String title) {
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "change_task_title")) {
            throw new SecurityException("Недостаточно прав для смены названия задачи");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Данной задачи не существует"));

        if (!isUserInSameCompany(user, task)) {
            throw new SecurityException("Вы можете менять данные задачи только в своей компании");
        }

        if (!task.getReporter().getId().equals(user.getId())){
            throw new SecurityException("Только тот кто поставил задачу может менять название задачи");
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

        if (!schemePermissionService.hasPermission(user, "change_task_priority")) {
            throw new SecurityException("Недостаточно прав для смены приоритета задачи");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Данной задачи не существует"));

        if (!isUserInSameCompany(user, task)) {
            throw new SecurityException("Вы можете менять данные задачи только в своей компании");
        }

        if (!task.getReporter().getId().equals(user.getId())){
            throw new SecurityException("Только тот кто поставил задачу может менять её приоритет");
        }

        if (task.getPriority().equals(priority)) {
            throw new IllegalArgumentException("Приоритет не изменился");
        }

        taskHistoryService.logTaskChange(task, user, "Priority",
                task.getPriority().toString(), priority.toString());
        task.setPriority(priority);

        return convertTaskToDTO(taskRepository.save(task));
    }

    public TaskDTO changeDescription(Long taskId, String description) {
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "change_task_description")) {
            throw new SecurityException("Недостаточно прав для смены описания задачи");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Данной задачи не существует"));

        if (!isUserInSameCompany(user, task)) {
            throw new SecurityException("Вы можете менять данные задачи только в своей компании");
        }

        if (!task.getAssignee().getId().equals(user.getId()) &&
                !schemePermissionService.hasPermission(user, "edit_any_task_description")) {
            throw new SecurityException("Только исполнитель задачи может менять её описание");
        }

        if (task.getDescription().equals(description)) {
            throw new IllegalArgumentException("Описание не изменилось");
        }

        taskHistoryService.logTaskChange(task, user, "Description",
                task.getDescription(), description);
        task.setDescription(description);

        return convertTaskToDTO(taskRepository.save(task));
    }

    public TaskDTO getTaskById(Long taskId) {
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "view_task")) {
            throw new SecurityException("Недостаточно прав для просмотра задачи");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        if (!isUserInSameCompany(user, task)) {
            throw new SecurityException("Вы не можете смотреть задачи другой компании");
        }

        if (!hasAccessToTask(user, task)) {
            throw new SecurityException("Нет доступа к этой задаче");
        }

        return convertTaskToDTO(task);
    }

    public Task GetTaskById(Long taskId) {
        User user = getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        if (!isUserInSameCompany(user, task)) {
            throw new SecurityException("Вы не можете смотреть задачи другой компании");
        }

        return task;
    }

    public List<TaskDTO> getTasksByAssignee(){
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "view_my_tasks")) {
            throw new SecurityException("Недостаточно прав для просмотра своих задач");
        }

        return taskRepository.findByAssignee(user).stream()
                .map(this::convertTaskToDTO)
                .toList();
    }

    public List<TaskDTO> getTasksByReporter(){
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "view_created_tasks")) {
            throw new SecurityException("Недостаточно прав для просмотра созданных задач");
        }

        return taskRepository.findByReporter(user).stream()
                .map(this::convertTaskToDTO)
                .toList();
    }

    public List<TaskDTO> getTasksByDepartment(){
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "view_department_tasks")) {
            throw new SecurityException("Недостаточно прав для просмотра задач отдела");
        }

        if (user.getDepartment() == null) {
            throw new IllegalArgumentException("У пользователя не указан отдел");
        }

        return taskRepository.findByDepartment(user.getDepartment()).stream()
                .map(this::convertTaskToDTO)
                .toList();
    }

    public List<TaskDTO> getTasksByProjectId(Long projectId){
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "view_project_tasks")) {
            throw new SecurityException("Недостаточно прав для просмотра задач проекта");
        }

        Project project = projectRepository.findByIdAndDepartment_Company(
                        projectId, user.getCompany())
                .orElseThrow(() -> new RuntimeException("Проект не найден в вашей компании"));

        return taskRepository.findByProject(project).stream()
                .map(this::convertTaskToDTO)
                .toList();
    }

    public List<TaskDTO> getSubTasksByTaskId(Long taskId){
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "view_subtasks")) {
            throw new SecurityException("Недостаточно прав для просмотра подзадач");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Данной задачи не существует"));

        if (!isUserInSameCompany(user, task)) {
            throw new SecurityException("Вы не можете получить задачи другой компании");
        }

        if (!hasAccessToTask(user, task)) {
            throw new SecurityException("Нет доступа к этой задаче");
        }

        List<Task> subtasks = task.getSubtasks();
        return subtasks.stream()
                .map(this::convertTaskToDTO)
                .toList();
    }

    public List<TaskDTO> getAllTasksInCompany(){
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "view_all_company_tasks")) {
            throw new SecurityException("Недостаточно прав для просмотра всех задач компании");
        }

        List<Task> tasks;

        if (user.getSystemRole() == SystemRole.COMPANY_OWNER) {
            // Владелец видит все задачи компании
            tasks = taskRepository.findByProject_Department_Company(user.getCompany());
        } else if (user.getSystemRole() == SystemRole.DEPARTMENT_HEAD && user.getDepartment() != null) {
            // Руководитель отдела видит задачи своего отдела
            tasks = taskRepository.findByDepartment(user.getDepartment());
        } else {
            // Обычный пользователь видит только доступные ему задачи
            tasks = taskRepository.findByAssigneeOrReporter(user);
        }

        return tasks.stream()
                .map(this::convertTaskToDTO)
                .toList();
    }

    public void deleteTask(Long taskId){
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "delete_task")) {
            throw new SecurityException("Недостаточно прав для удаления задачи");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Данной задачи не существует"));

        if (!isUserInSameCompany(user, task)) {
            throw new SecurityException("Вы не можете получить задачи другой компании");
        }

        if (!task.getReporter().getId().equals(user.getId()) &&
                !schemePermissionService.hasPermission(user, "delete_any_task")) {
            throw new SecurityException("Только создатель может удалить задачу");
        }

        taskHistoryService.logTaskChange(task, user, "Delete Task", task.getTitle(), task.getTitle());
        taskRepository.delete(task);
    }

    public boolean existsByAssigneeAndId(User user, Long taskId){
        return taskRepository.existsByAssigneeAndId(user, taskId);
    }

    private boolean isUserInSameCompany(User user, Task task) {
        if (user.getCompany() == null || task.getProject() == null ||
                task.getProject().getDepartment() == null ||
                task.getProject().getDepartment().getCompany() == null) {
            return false;
        }

        return user.getCompany().getId()
                .equals(task.getProject().getDepartment().getCompany().getId());
    }

    private boolean hasAccessToTask(User user, Task task) {
        if (user.getSystemRole() == SystemRole.COMPANY_OWNER) {
            return true;
        }

        if (user.getSystemRole() == SystemRole.DEPARTMENT_HEAD &&
                user.getDepartment() != null &&
                task.getProject().getDepartment() != null) {
            return user.getDepartment().getId()
                    .equals(task.getProject().getDepartment().getId());
        }

        boolean isAssignee = task.getAssignee() != null &&
                task.getAssignee().getId().equals(user.getId());

        boolean isReporter = task.getReporter() != null &&
                task.getReporter().getId().equals(user.getId());

        boolean hasAccessPermission = schemePermissionService
                .hasPermission(user, "view_all_tasks");

        return isAssignee || isReporter || hasAccessPermission;
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
        taskDTO.setDescription(task.getDescription());
        taskDTO.setStart(task.getStartTime());
        taskDTO.setEnd(task.getEndTime());
        taskDTO.setStatus(task.getStatus());
        taskDTO.setPriority(task.getPriority());

        if (task.getReporter() != null) {
            taskDTO.setReporterId(task.getReporter().getId()); // ← Теперь есть сеттер
            taskDTO.setReporterName(task.getReporter().getUserName());
        }

        if (task.getAssignee() != null) {
            taskDTO.setAssigneeId(task.getAssignee().getId());
            taskDTO.setAssigneeName(task.getAssignee().getUserName());
        }

        if (task.getProject() != null) {
            taskDTO.setProjectId(task.getProject().getId());
            taskDTO.setProjectName(task.getProject().getName());
        }

        if (task.getParentTask() != null) {
            taskDTO.setParentTaskId(task.getParentTask().getId());
        }

        return taskDTO;
    }
}