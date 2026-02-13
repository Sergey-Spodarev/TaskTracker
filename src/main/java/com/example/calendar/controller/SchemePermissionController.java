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

    @PostMapping("/schemes/{schemeId}/permissions")
    public ResponseEntity<SchemePermissionDTO> createSchemePermission(@PathVariable Long schemeId, @RequestBody SchemePermissionDTO schemePermissionDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(schemePermissionService.createSchemePermission(schemeId, schemePermissionDTO));
    }

    @GetMapping("/schemes/{schemeId}/permissions")
    public ResponseEntity<List<SchemePermissionDTO>> getSchemePermissions(@PathVariable Long schemeId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(schemePermissionService.getSchemePermissions(schemeId));
    }

    @GetMapping("/schemes/{schemeId}/permissions/{permissionId}")
    public ResponseEntity<SchemePermissionDTO> getSchemePermission(@PathVariable Long schemeId, @PathVariable Long permissionId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(schemePermissionService.getSchemePermission(schemeId, permissionId));
    }

    @PutMapping("/{schemeId}/permissions/{permissionId}")
    public ResponseEntity<SchemePermissionDTO> updateSchemePermission(@PathVariable Long schemeId, @PathVariable Long permissionId, @RequestBody SchemePermissionDTO updateSchemePermissionDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(schemePermissionService.updateSchemePermission(schemeId, permissionId, updateSchemePermissionDTO));
    }

    @DeleteMapping("/{schemeId}/permissions/{permissionId}")
    public ResponseEntity<Void> deleteSchemePermission(@PathVariable Long schemeId, @PathVariable Long permissionId) {
        schemePermissionService.deleteSchemePermission(schemeId, permissionId);
        return ResponseEntity.noContent().build();
    }
}
