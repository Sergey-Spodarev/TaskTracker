package com.example.calendar.controller;

import com.example.calendar.DTO.SystemPermissionCatalogDTO;
import com.example.calendar.service.SystemPermissionCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system-permissions")
public class SystemPermissionCatalogController {
    private final SystemPermissionCatalogService systemPermissionCatalogService;
    public SystemPermissionCatalogController(SystemPermissionCatalogService systemPermissionCatalogService) {
        this.systemPermissionCatalogService = systemPermissionCatalogService;
    }

    @PatchMapping("/update")
    public ResponseEntity<SystemPermissionCatalogDTO> updateSystemPermissionCatalog(@RequestBody SystemPermissionCatalogDTO systemPermissionCatalogDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(systemPermissionCatalogService.updateSystemPermissionCatalog(systemPermissionCatalogDTO));
    }

    @GetMapping("/by_name")
    public ResponseEntity<SystemPermissionCatalogDTO> getSystemPermissionCatalogByName(@RequestParam String name) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(systemPermissionCatalogService.getSystemPermissionCatalogByName(name));
    }

    @GetMapping
    public ResponseEntity<List<SystemPermissionCatalogDTO>> getAllSystemPermissionCatalog() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(systemPermissionCatalogService.getAllSystemPermissionCatalog());
    }
}
