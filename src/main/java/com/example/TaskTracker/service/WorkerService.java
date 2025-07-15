package com.example.TaskTracker.service;

import com.example.TaskTracker.DTO.TasksUserDTO;
import com.example.TaskTracker.model.Tasks;
import com.example.TaskTracker.model.Users;
import com.example.TaskTracker.repository.AdminRepository;
import com.example.TaskTracker.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WorkerService {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    public WorkerService(AdminRepository adminRepository, UserRepository userRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    public List<TasksUserDTO> getTask(String email) {
        Users user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return user.getAssignedTasks().stream()
                .map(this::convertTaskToDTO)
                .toList();
    }

    private TasksUserDTO convertTaskToDTO(Tasks task) {
        TasksUserDTO dto = new TasksUserDTO();

        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStartDate(task.getStartDate());
        dto.setEndDate(task.getEndData());
        dto.setPriority(task.getPriority());
        dto.setAssigneeEmail(task.getAssignee().getEmail());

        if (task.getStatus() != null) {
            dto.setStatus(task.getStatus());
            dto.setComment(task.getComment());
            dto.setUpdatedAt(task.getUpdatedAt());
        } else {
            dto.setStatus("не начата");
            dto.setComment("");
            dto.setUpdatedAt(null);
        }

        return dto;
    }

    public Users getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    public TasksUserDTO updateTask(TasksUserDTO tasksUserDTO) {
        Tasks updateTasks = adminRepository.findById(tasksUserDTO.getId())
                .orElseThrow(() -> new RuntimeException("новая"));

        updateTasks.setStartDate(tasksUserDTO.getStartDate());
        updateTasks.setEndData(tasksUserDTO.getEndDate());
        updateTasks.setPriority(tasksUserDTO.getPriority());
        updateTasks.setDescription(tasksUserDTO.getDescription());
        updateTasks.setTitle(tasksUserDTO.getTitle());
        updateTasks.setAssignee(getUser(tasksUserDTO.getAssigneeEmail()));
        updateTasks.setStatus(tasksUserDTO.getStatus());
        updateTasks.setComment(tasksUserDTO.getComment());

        Tasks updatedTask = adminRepository.save(updateTasks);

        return convertTaskToDTO(updatedTask);
    }
}
