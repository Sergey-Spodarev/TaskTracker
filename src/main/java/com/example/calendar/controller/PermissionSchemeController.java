package com.example.calendar.controller;

import com.example.calendar.DTO.PermissionSchemeDTO;
import com.example.calendar.service.PermissionSchemeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/permission-schemes")
public class PermissionSchemeController {
    private final PermissionSchemeService permissionSchemeService;
    public PermissionSchemeController(PermissionSchemeService permissionSchemeService) {
        this.permissionSchemeService = permissionSchemeService;
    }

    @PostMapping("/add")
    public ResponseEntity<PermissionSchemeDTO> addPermissionScheme(@RequestBody PermissionSchemeDTO permissionSchemeDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(permissionSchemeService.addPermissionScheme(permissionSchemeDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PermissionSchemeDTO>> getAllPermissionScheme() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(permissionSchemeService.getAllPermissionSchemes());
    }

    @GetMapping("/{schemeId}")
    public ResponseEntity<PermissionSchemeDTO> getPermissionScheme(@PathVariable Long schemeId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(permissionSchemeService.getPermissionScheme(schemeId));
    }

    @PutMapping("/{schemeId}/update")
    public ResponseEntity<PermissionSchemeDTO> updatePermissionScheme(@PathVariable Long schemeId, @RequestBody PermissionSchemeDTO permissionSchemeDTO) {//так надо сделать новый DTO чтобы не было некоторых полей которые нельзя менять
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(permissionSchemeService.updatePermissionScheme(schemeId, permissionSchemeDTO));
    }

    @DeleteMapping("/{schemeId}/delete")
    public ResponseEntity<Void> deletePermissionScheme(@PathVariable Long schemeId) {
        permissionSchemeService.deletePermissionScheme(schemeId);
        return ResponseEntity.noContent().build();
    }
}
