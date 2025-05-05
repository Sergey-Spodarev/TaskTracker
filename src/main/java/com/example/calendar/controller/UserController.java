package com.example.calendar.controller;

import ch.qos.logback.core.model.Model;
import com.example.calendar.DTO.UserDTO;
import com.example.calendar.model.User;
import com.example.calendar.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username, @RequestParam String email, @RequestParam String password, Model model) {
        try {
            UserDTO userDTO = new UserDTO();
            userDTO.setUserName(username);
            userDTO.setEmail(email);
            userDTO.setPassword(password);
            User registeredUser = userService.registerUser(userDTO);
            return "redirect:/login?success";
        } catch (Exception e) {
            return "register";
        }
    }
}
