package com.example.calendar.controller;

import com.example.calendar.DTO.DepartmentDTO;
import com.example.calendar.DTO.TaskAssignmentPermissionDTO;
import com.example.calendar.service.TaskAssignmentPermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/task_assignment_permission")
public class TaskAssignmentPermissionController {
    private final TaskAssignmentPermissionService taskAssignmentPermissionService;
    public TaskAssignmentPermissionController(TaskAssignmentPermissionService taskAssignmentPermissionService) {
        this.taskAssignmentPermissionService = taskAssignmentPermissionService;
    }

    @PostMapping("/create")
    public ResponseEntity<TaskAssignmentPermissionDTO> createTaskAssignmentPermission(@Validated @RequestBody TaskAssignmentPermissionDTO taskAssignmentPermissionDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskAssignmentPermissionService.createTaskAssignmentPermission(taskAssignmentPermissionDTO));
    }

    @PutMapping("/update/{assignment_permission_id}")
    public ResponseEntity<TaskAssignmentPermissionDTO> update(@Validated TaskAssignmentPermissionDTO taskAssignmentPermissionDTO,
                                                              @PathVariable Long assignment_permission_id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskAssignmentPermissionService.update(taskAssignmentPermissionDTO, assignment_permission_id));
    }

    @GetMapping("/get-by/{id}")
    public ResponseEntity<TaskAssignmentPermissionDTO> getTaskAssignmentPermissionById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskAssignmentPermissionService.getTaskAssignmentPermission(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<TaskAssignmentPermissionDTO>> getAllTaskAssignmentPermission() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskAssignmentPermissionService.getTaskAssignmentPermissions());
    }

    @GetMapping("/department/{department_id}")
    public ResponseEntity<List<TaskAssignmentPermissionDTO>> getTaskAssignmentPermissionByDepartment(@PathVariable Long department_id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskAssignmentPermissionService.getTaskAssignmentPermissionsByDepartment(department_id));
    }

    @GetMapping("/role-level/{id}")
    public ResponseEntity<List<TaskAssignmentPermissionDTO>> getTaskAssignmentPermissionByRole(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskAssignmentPermissionService.getTaskAssignmentPermissionsByRoleLevel(id));
    }

    @GetMapping("/my-available-departments")
    public ResponseEntity<List<DepartmentDTO>> getMyAvailableDepartments() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(taskAssignmentPermissionService.getMyAvailableDepartments());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteTaskAssignmentPermission(@PathVariable Long id) {
        taskAssignmentPermissionService.deleteTaskAssignmentPermission(id);
        return ResponseEntity.noContent().build();
    }
}
