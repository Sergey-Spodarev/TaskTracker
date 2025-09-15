package com.example.calendar.controller;

import com.example.calendar.DTO.ProjectDTO;
import com.example.calendar.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
public class ProjectController {
    private final ProjectService projectService;
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/create")
    public ResponseEntity<ProjectDTO> createProject(@RequestBody @Valid ProjectDTO projectDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.create(projectDTO));
    }

    @PutMapping("/update")
    public ResponseEntity<ProjectDTO> updateProject(@RequestBody @Valid ProjectDTO projectDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.update(projectDTO));
    }

    @GetMapping("/getAll")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
