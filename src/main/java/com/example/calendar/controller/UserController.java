package com.example.calendar.controller;

import com.example.calendar.DTO.UserDTO;
import com.example.calendar.model.User;
import com.example.calendar.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//Контроллеры (обработка HTTP-запросов):работа с календарём.
@RestController
@RequestMapping("/api/user")//Эта аннотация задает базовый URL для всех методов в классе контроллера
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO userDTO){
        User registeredUser = userService.registerUser(userDTO);
        return ResponseEntity.ok()
                .body("Пользователь успешно зарегистрирован. ID: " + registeredUser.getUserId());
    }
}
