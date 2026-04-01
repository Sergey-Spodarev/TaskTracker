package com.example.calendar.controller;

import com.example.calendar.DTO.CreateRoleLevelDTO;
import com.example.calendar.DTO.RoleLevelDTO;
import com.example.calendar.service.RoleLevelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role-levels")
public class RoleLevelController {
    private final RoleLevelService roleLevelService;
    public RoleLevelController(RoleLevelService roleLevelService) {
        this.roleLevelService = roleLevelService;
    }

    @PostMapping("/create")
    public ResponseEntity<RoleLevelDTO> create(@RequestBody CreateRoleLevelDTO roleLevelDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roleLevelService.create(roleLevelDTO));
    }

    @PatchMapping("/update")
    public ResponseEntity<RoleLevelDTO> update(@RequestBody RoleLevelDTO roleLevelDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleLevelService.update(roleLevelDTO));
    }

    @PatchMapping("/{roleLevelId}/permissions")
    public ResponseEntity<Void> updateRoleLevelPermissions(@PathVariable Long roleLevelId, @RequestBody List<String> permissions) {
        roleLevelService.updateRoleLevelPermissions(roleLevelId, permissions);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{levelId}/update")
    public ResponseEntity<RoleLevelDTO> updateRoleLevel(@PathVariable Long levelId, @RequestBody RoleLevelDTO request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleLevelService.updateRoleLevel(levelId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleLevelDTO> getById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleLevelService.getById(id));
    }

    @GetMapping("/role/{roleCode}/levels")
    public ResponseEntity<List<RoleLevelDTO>> getRoleLevelsByRole(@PathVariable String roleCode) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleLevelService.getRoleLevelsByRole(roleCode));
    }

    @GetMapping("/{roleLevelId}/permissions")
    public ResponseEntity<List<String>> getRoleLevelPermissions(@PathVariable Long roleLevelId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleLevelService.getRoleLevelPermissions(roleLevelId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoleLevelDTO>> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleLevelService.getAll());
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<RoleLevelDTO> delete(@PathVariable Long id) {
        roleLevelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
