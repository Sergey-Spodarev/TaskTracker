package com.example.calendar.service;

import com.example.calendar.DTO.UserDTO;
import com.example.calendar.model.User;
import com.example.calendar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Бизнес-логика (например, UserService для регистрации).
@Service // Помечает класс как сервисный компонент (логика приложения)
@Transactional //Управляет транзакциями для работы с БД.
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Boolean checkUserRegistered(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    public User registerUser(UserDTO userDTO){
        if (checkUserRegistered(userDTO.getEmail())){
            throw new RuntimeException("Пользователь уже существует");//временно так потом надо сообщение пользователю выбивать
        }

        User user = new User();
        user.setUserName(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());

        return userRepository.save(user);
    }

//    public User updatePassword(String email, String newPassword){
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
//        user.setPassword(newPassword);
//        return userRepository.save(user);
//    }
}
