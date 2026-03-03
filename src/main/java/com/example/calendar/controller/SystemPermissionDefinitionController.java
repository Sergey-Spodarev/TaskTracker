package com.example.calendar.controller;

import com.example.calendar.DTO.SystemPermissionDefinitionDTO;
import com.example.calendar.DTO.SystemPermissionDefinitionUpdateDTO;
import com.example.calendar.service.SystemPermissionDefinitionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permission-definition")
public class SystemPermissionDefinitionController {
    private final SystemPermissionDefinitionService permissionDefinitionService;
    public SystemPermissionDefinitionController(SystemPermissionDefinitionService permissionDefinitionService) {
        this.permissionDefinitionService = permissionDefinitionService;
    }

    @PostMapping("/create")
    public ResponseEntity<SystemPermissionDefinitionDTO> saveSystemPermissionDefinition(@RequestBody SystemPermissionDefinitionDTO systemPermissionDefinitionDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(permissionDefinitionService.createSystemPermissionDefinition(systemPermissionDefinitionDTO));
    }

    @PatchMapping("/update-data")
    public ResponseEntity<SystemPermissionDefinitionDTO> updateSystemPermissionDefinition(@RequestBody SystemPermissionDefinitionDTO systemPermissionDefinitionDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(permissionDefinitionService.updateSystemPermissionDefinition(systemPermissionDefinitionDTO));
    }

    @PatchMapping("/add-permission")
    public ResponseEntity<SystemPermissionDefinitionDTO> addSystemPermissionDefinition(@RequestBody SystemPermissionDefinitionUpdateDTO systemPermissionDefinitionUpdateDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(permissionDefinitionService.addSystemPermissionDefinition(systemPermissionDefinitionUpdateDTO));
    }

    @PatchMapping("delete-permission")
    public ResponseEntity<SystemPermissionDefinitionDTO> deleteSystemPermissionDefinition(@RequestBody SystemPermissionDefinitionUpdateDTO systemPermissionDefinitionUpdateDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(permissionDefinitionService.deleteSystemPermissionDefinition(systemPermissionDefinitionUpdateDTO));
    }

    @GetMapping("/{code}")
    public ResponseEntity<SystemPermissionDefinitionDTO> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(permissionDefinitionService.getByCode(code));
    }

    @GetMapping
    public ResponseEntity<List<SystemPermissionDefinitionDTO>> getAll() {
        return ResponseEntity.ok(permissionDefinitionService.getAll());
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        permissionDefinitionService.deleteByCode(code);
        return ResponseEntity.noContent().build();
    }
}
