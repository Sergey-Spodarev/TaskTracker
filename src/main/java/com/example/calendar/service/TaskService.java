package com.example.calendar.service;

import com.example.calendar.DTO.TaskDTO;
import com.example.calendar.model.Project;
import com.example.calendar.model.Task;
import com.example.calendar.model.User;
import com.example.calendar.repository.ProjectRepository;
import com.example.calendar.repository.TaskRepository;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    public TaskDTO createTask(TaskDTO taskDTO) {
        User user = getCurrentUser();
        //тут надо будет потом дописать ещё проверку на права, ибо не любой может дать задание любому у меня есть специальный класс для этого
        Project project = projectRepository.findByIdAndCompany(taskDTO.getProjectId(), user.getCompany())
                .orElseThrow(() -> new UsernameNotFoundException("Нельзя дать задачу не своей группе"));

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setStartTime(taskDTO.getStart());
        task.setEndTime(taskDTO.getEnd());
        task.setReporter(user);
        task.setProject(project);
        User assigneeUser = userRepository.findById(taskDTO.getAssigneeId())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        task.setAssignee(assigneeUser);
        return convertTaskToDTO(taskRepository.save(task));
    }

    public List<TaskDTO> getAllTasks() {
        User user = getCurrentUser();
        return taskRepository.findByProject(user.getCompany().getProjects())
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
