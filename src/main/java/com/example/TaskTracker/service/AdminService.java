package com.example.TaskTracker.service;

import com.example.TaskTracker.DTO.TasksDTO;
import com.example.TaskTracker.DTO.UserDTO;
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
        return convertToDTO(savedTask);
    }

    public Users getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    private TasksDTO convertToDTO(Tasks task) {
        TasksDTO dto = new TasksDTO();
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStartDate(task.getStartDate());
        dto.setEndDate(task.getEndData());
        dto.setPriority(task.getPriority());
        dto.setAssigneeEmail(task.getAssignee().getEmail());
        return dto;
    }

    private UserDTO convertToDTO(Users user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }

    public List<UserDTO> getAllUsersWithTasks() {//todo переделать чтобы возвращал значения типа TasksDTO
        List<Users> usersWithTasks = userRepository.findUsersWithTasks();
        return usersWithTasks.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<UserDTO> getAllUsers() {
        List<Users> allUsers = userRepository.findAllByRole("USER");
        return allUsers.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public void delTask(TasksDTO tasksDTO){
        adminRepository.delete(adminRepository.findById(tasksDTO.getId()).get());
    }
}
