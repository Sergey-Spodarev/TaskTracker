package com.example.calendar.controller;

import com.example.calendar.DTO.CreateRoleLevelDTO;
import com.example.calendar.DTO.RoleLevelDTO;
import com.example.calendar.service.RoleLevelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/role_level")
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

    @GetMapping("/{id}")
    public ResponseEntity<RoleLevelDTO> getById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleLevelService.getById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<RoleLevelDTO>> getAll() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleLevelService.getAll());
    }

    @PatchMapping("/update")
    public ResponseEntity<RoleLevelDTO> update(@RequestBody RoleLevelDTO roleLevelDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(roleLevelService.update(roleLevelDTO));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<RoleLevelDTO> delete(@PathVariable Long id) {
        roleLevelService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
