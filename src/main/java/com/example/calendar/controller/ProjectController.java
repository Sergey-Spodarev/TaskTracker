package com.example.calendar.controller;

import com.example.calendar.DTO.ProjectDTO;
import com.example.calendar.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/project")
public class ProjectController {
    private final ProjectService projectService;
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/create")
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.create(projectDTO));
    }

    @PutMapping("/update")
    public ResponseEntity<ProjectDTO> updateProject(@RequestBody ProjectDTO projectDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(projectService.update(projectDTO));
    }

    @GetMapping("/getAll")
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
