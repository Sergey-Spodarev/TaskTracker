package com.example.calendar.controller;

import com.example.calendar.DTO.RoleDTO;
import com.example.calendar.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping("/all")//это у админа только
    public ResponseEntity<List<String>> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.findAllRoles());
    }

    @PostMapping("/add")//это тоже для админа
    public ResponseEntity<RoleDTO> add(@RequestBody RoleDTO roleDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleService.addRole(roleDTO));
    }

    @PostMapping("/updateRole")//это у админа только
    public ResponseEntity<RoleDTO> update(@RequestBody RoleDTO roleDTO, @RequestParam String userName) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleService.updateRole(roleDTO, userName));
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> delete(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }
}
