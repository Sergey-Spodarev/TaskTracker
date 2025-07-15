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
        dto.setId(task.getId());
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

    public List<TasksDTO> getAllTasks() {
        List<Tasks> allUsers = adminRepository.findAll();
        return allUsers.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<UserDTO> getAllUsers() {
        List<Users> allUsers = userRepository.findAllByRole("USER");
        return allUsers.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public void delTask(Long id){
        adminRepository.deleteById(id);
    }

    public TasksDTO updateTask(TasksDTO tasksDTO, String currentEmail) {
        Tasks updateTasks = adminRepository.findById(tasksDTO.getId())
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        updateTasks.setStartDate(tasksDTO.getStartDate());
        updateTasks.setEndData(tasksDTO.getEndDate());
        updateTasks.setPriority(tasksDTO.getPriority());
        updateTasks.setDescription(tasksDTO.getDescription());
        updateTasks.setTitle(tasksDTO.getTitle());

        updateTasks.setAssignee(getUser(tasksDTO.getAssigneeEmail()));
        updateTasks.setCreator(getUser(currentEmail));

        Tasks updatedTask = adminRepository.save(updateTasks);

        return convertToDTO(updatedTask);
    }
}
