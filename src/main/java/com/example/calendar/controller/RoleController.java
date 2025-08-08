package com.example.calendar.controller;

import com.example.calendar.repository.RoleRepository;
import com.example.calendar.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/role")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService, RoleRepository roleRepository) {
        this.roleService = roleService;
    }

    @GetMapping("/all")//это у админа только
    public ResponseEntity<List<String>> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.findAllRoles());
    }
}
