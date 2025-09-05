package com.example.calendar.controller;

import com.example.calendar.DTO.DepartmentDTO;
import com.example.calendar.service.DepartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/department")
public class DepartmentController {
    private final DepartmentService departmentService;
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping("/addDepartment")
    public ResponseEntity<DepartmentDTO> addNewDepartment(@RequestBody DepartmentDTO departmentDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(departmentService.createDepartment(departmentDTO));
    }

    @PutMapping("/updateName")
    public ResponseEntity<DepartmentDTO> updateName(@RequestBody DepartmentDTO departmentDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(departmentService.updateName(departmentDTO));
    }

    @DeleteMapping("/{department_id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long department_id) {
        departmentService.deleteDepartment(department_id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get")
    public ResponseEntity<List<DepartmentDTO>> getDepartments() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(departmentService.getAllDepartments());
    }
}
