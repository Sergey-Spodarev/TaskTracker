package com.example.calendar.controller;

import com.example.calendar.DTO.LoginDto;
import com.example.calendar.model.User;
import com.example.calendar.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class AuthController {
    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        System.out.println("[DEBUG] Получен запрос: " + loginDto.getUserName() + "/" + loginDto.getPassword());

        User user = userRepository.findByUserName(loginDto.getUserName())
                .orElseThrow(() -> {
                    System.out.println("[DEBUG] Пользователь не найден: " + loginDto.getUserName());
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                });

        System.out.println("[DEBUG] Найден пользователь: " + user.getUserName() + "/" + user.getPassword());

        if (!user.getPassword().equals(loginDto.getPassword())) {
            System.out.println("[DEBUG] Пароль не совпадает!");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/calendar")
    public String showCalendar() {
        return "forward:/calendar.html"; // Или имя вашего HTML-файла с календарём
    }
}
