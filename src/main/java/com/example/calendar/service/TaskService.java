package com.example.calendar.service;

import com.example.calendar.DTO.TaskDTO;
import com.example.calendar.model.Company;
import com.example.calendar.model.Project;
import com.example.calendar.model.Task;
import com.example.calendar.model.User;
import com.example.calendar.repository.CompanyRepository;
import com.example.calendar.repository.ProjectRepository;
import com.example.calendar.repository.TaskRepository;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final AssignmentRuleService assignmentRuleService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, ProjectRepository projectRepository, AssignmentRuleService assignmentRuleService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.assignmentRuleService = assignmentRuleService;
    }
    /*
    надо написать функцию чтобы исполнитель мог меняться

    потом сделать новую таблицу чтобы можно было делать под задачи, чтобы одну главную задачу можно разбить
    Пример: задача крупная надо что-то разбить на фронт и бэк и они должны тоже закрыться
    и пока под задачи не закроются нельзя закрыть основную
     */

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
        return convertTaskToDTO(taskRepository.save(task));
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

    public List<TaskDTO> getAllTasksInCompany(){
        User user = getCurrentUser();
        Company company = user.getCompany();
        List<Task> task = taskRepository.findByProject_Company(company);
        return task.stream()
                .map(this::convertTaskToDTO)
                .toList();
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
        return taskDTO;
    }
}
