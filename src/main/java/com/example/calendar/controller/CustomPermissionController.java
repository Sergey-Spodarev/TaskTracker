package com.example.calendar.controller;

import com.example.calendar.DTO.CustomPermissionDTO;
import com.example.calendar.DTO.CustomPermissionUpdateDTO;
import com.example.calendar.service.CustomPermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/custom-permission")
public class CustomPermissionController {
    private final CustomPermissionService customPermissionService;
    public CustomPermissionController(CustomPermissionService customPermissionService) {
        this.customPermissionService = customPermissionService;
    }

    @PostMapping("/create")
    public ResponseEntity<CustomPermissionDTO> create(@RequestBody CustomPermissionDTO customPermissionDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customPermissionService.createPermission(customPermissionDTO));
    }

    @PatchMapping("/update")
    public ResponseEntity<CustomPermissionDTO> update(@RequestBody CustomPermissionDTO customPermissionDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customPermissionService.updatePermission(customPermissionDTO));
    }

    @PatchMapping("/add")
    public ResponseEntity<CustomPermissionDTO> add(@RequestBody CustomPermissionUpdateDTO customPermissionUpdateDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customPermissionService.addPermission(customPermissionUpdateDTO));
    }

    @GetMapping("/{id}/get")
    public ResponseEntity<CustomPermissionDTO> getPermission(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customPermissionService.getPermissionById(id));
    }

    @GetMapping("/get-code")
    public ResponseEntity<CustomPermissionDTO> getPermissionCode(String code) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customPermissionService.getPermissionByCode(code));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CustomPermissionDTO>> getAllPermission() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(customPermissionService.getAllPermissions());
    }

    @DeleteMapping("/{code}/del")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        customPermissionService.deletePermission(code);
        return ResponseEntity.noContent().build();
    }
}
