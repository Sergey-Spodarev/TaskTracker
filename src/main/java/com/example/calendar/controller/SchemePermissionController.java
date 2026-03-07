package com.example.calendar.controller;

import com.example.calendar.DTO.SchemePermissionDTO;
import com.example.calendar.service.SchemePermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class SchemePermissionController {
    private final SchemePermissionService schemePermissionService;
    public SchemePermissionController(SchemePermissionService schemePermissionService) {
        this.schemePermissionService = schemePermissionService;
    }

    @PostMapping("/schemes")
    public ResponseEntity<SchemePermissionDTO> createSchemePermission(@RequestBody SchemePermissionDTO schemePermissionDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(schemePermissionService.createSchemePermission(schemePermissionDTO));
    }

    @PatchMapping("/update")
    public ResponseEntity<SchemePermissionDTO> updateSchemePermission(@RequestBody SchemePermissionDTO updateSchemePermissionDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(schemePermissionService.updateSchemePermission(updateSchemePermissionDTO));
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<SchemePermissionDTO> getSchemePermissionById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(schemePermissionService.getSchemePermission(id));
    }

    @GetMapping("/get/{permissionKey}")
    public ResponseEntity<SchemePermissionDTO> getSchemePermissions(@PathVariable String permissionKey) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(schemePermissionService.getSchemePermissionByKey(permissionKey));
    }

    @GetMapping("/getALL")
    public ResponseEntity<List<SchemePermissionDTO>> getAllSchemePermissions() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(schemePermissionService.getAllSchemePermissions());
    }

    @DeleteMapping("/del/{permissionKey}")
    public ResponseEntity<Void> delSchemePermission(@PathVariable String permissionKey) {
        schemePermissionService.deleteSchemePermission(permissionKey);
        return ResponseEntity.noContent().build();
    }
}
