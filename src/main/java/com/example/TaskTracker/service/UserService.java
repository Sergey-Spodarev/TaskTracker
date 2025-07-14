package com.example.TaskTracker.service;


import com.example.TaskTracker.DTO.UserDTO;
import com.example.TaskTracker.model.Users;
import com.example.TaskTracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Boolean haveUser(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    public UserDTO saveUser(UserDTO userDTO){//todo надо может будет сделать что сюда передавать роль чтобы она тут добавлялась а не в Controller
        if(haveUser(userDTO.getEmail())){
            throw new RuntimeException("Пользователь уже существует");
        }
        Users user = new Users();
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(userDTO.getRole());
        Users savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    private UserDTO convertToDTO(Users user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        // Пароль НЕ возвращаем!
        return dto;
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }
}
