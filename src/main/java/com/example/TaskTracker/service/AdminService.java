package com.example.TaskTracker.service;

import com.example.TaskTracker.DTO.TasksDTO;
import com.example.TaskTracker.model.Tasks;
import com.example.TaskTracker.model.Users;
import com.example.TaskTracker.repository.AdminRepository;
import com.example.TaskTracker.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    public AdminService(AdminRepository adminRepository, UserRepository userRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    public TasksDTO saveTask(TasksDTO tasksDTO, String currentEmail) {
        Tasks tasks = new Tasks();

        tasks.setStartDate(tasksDTO.getStartDate());
        tasks.setEndData(tasksDTO.getEndDate());
        tasks.setPriority(tasksDTO.getPriority());
        tasks.setDescription(tasksDTO.getDescription());
        tasks.setTitle(tasksDTO.getTitle());

        tasks.setCreator(getUser(currentEmail));
        tasks.setAssignee(getUser(tasksDTO.getAssigneeEmail()));

        Tasks savedTask = adminRepository.save(tasks);
        return mapToDTO(savedTask);
    }

    public Users getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    private TasksDTO mapToDTO(Tasks task) {
        TasksDTO dto = new TasksDTO();
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStartDate(task.getStartDate());
        dto.setEndDate(task.getEndData());
        dto.setPriority(task.getPriority());
        dto.setAssigneeEmail(task.getAssignee().getEmail());
        return dto;
    }

}
