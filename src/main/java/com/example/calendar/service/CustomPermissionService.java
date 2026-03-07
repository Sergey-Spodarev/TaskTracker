package com.example.calendar.service;

import com.example.calendar.DTO.CustomPermissionDTO;
import com.example.calendar.DTO.CustomPermissionUpdateDTO;
import com.example.calendar.model.CustomPermission;
import com.example.calendar.model.SchemePermission;
import com.example.calendar.model.User;
import com.example.calendar.repository.CustomPermissionRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CustomPermissionService {
    private final CustomPermissionRepository customPermissionRepository;
    private final PermissionCheckService permissionCheckService;
    private final SchemePermissionService schemePermissionService;
    public CustomPermissionService(CustomPermissionRepository customPermissionRepository, PermissionCheckService permissionCheckService, SchemePermissionService schemePermissionService) {
        this.customPermissionRepository = customPermissionRepository;
        this.permissionCheckService = permissionCheckService;
        this.schemePermissionService = schemePermissionService;
    }

    public CustomPermissionDTO createPermission(CustomPermissionDTO customPermissionDTO) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "CREATE_CUSTOM_PERMISSION")) {
            throw new SecurityException("Нет права на создание кастомного права");
        }

        if (!customPermissionRepository.existsByCode(customPermissionDTO.getCode())) {
            throw new RuntimeException("Схема с таким кодом " + customPermissionDTO.getCode() + " уже существует.");
        }

        CustomPermission customPermission = new CustomPermission();
        customPermission.setCode(customPermissionDTO.getCode());
        customPermission.setName(customPermissionDTO.getName());
        customPermission.setDescription(customPermissionDTO.getDescription());
        customPermission.setCreateTime(LocalDateTime.now());
        customPermission.setCreatePermission(user);

        customPermissionRepository.save(customPermission);
        return convertToDTO(customPermission);
    }

    public CustomPermissionDTO updatePermission(CustomPermissionDTO customPermissionDTO) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "EDIT_CUSTOM_PERMISSION")) {
            throw new SecurityException("Нет права на редактирование кастомного права");
        }

        CustomPermission updateCustomPermission = customPermissionRepository.findByCode(customPermissionDTO.getCode())
                .orElseThrow(() -> new RuntimeException("Задачи с так кодом " + customPermissionDTO.getCode() + " не существует."));

        if (!updateCustomPermission.getCreatePermission().getCompany().equals(user.getCompany())) {
            throw new SecurityException("Нельзя редактировать право другой компании");
        }

        updateCustomPermission.setName(customPermissionDTO.getName());
        updateCustomPermission.setDescription(customPermissionDTO.getDescription());
        updateCustomPermission.setCreateTime(LocalDateTime.now());
        customPermissionRepository.save(updateCustomPermission);
        return convertToDTO(updateCustomPermission);
    }

    public CustomPermissionDTO addPermission(CustomPermissionUpdateDTO customPermissionUpdateDTO) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "ADD_SCHEME_LEVEL_TO_CUSTOM")) {
            throw new SecurityException("Нет права на добавление уровня к кастомному праву");
        }

        CustomPermission customPermission = customPermissionRepository.findByCode(customPermissionUpdateDTO.getCustomPermissionCode())
                .orElseThrow(() -> new RuntimeException("Задачи с так кодом " + customPermissionUpdateDTO.getCustomPermissionCode() + " не существует."));

        for (SchemePermission schemePermission : customPermission.getSchemePermission()) {
            if (schemePermission.getPermissionKey().equals(customPermissionUpdateDTO.getSchemePermissionCode())) {
                throw new RuntimeException("Такая задача " + customPermissionUpdateDTO.getSchemePermissionCode() + " уже добавлена в права.");
            }
        }

        SchemePermission schemePermission = schemePermissionService.getSchemePermissionByCode(customPermissionUpdateDTO.getSchemePermissionCode());
        customPermission.getSchemePermission().add(schemePermission);
        customPermission.setCreatePermission(user);
        customPermissionRepository.save(customPermission);
        return convertToDTO(customPermission);
    }

    public CustomPermissionDTO getPermissionByCode(String code) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "VIEW_CUSTOM_PERMISSIONS")) {
            throw new SecurityException("Нет права на просмотр кастомных прав");
        }

        CustomPermission customPermission = customPermissionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Задачи с так кодом " + code + " не существует."));
        return convertToDTO(customPermission);
    }

    public CustomPermissionDTO getPermissionById(Long id) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "VIEW_CUSTOM_PERMISSIONS")) {
            throw new SecurityException("Нет права на просмотр кастомных прав");
        }

        CustomPermission customPermission = customPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задачи аод таким номером " + id + " не существует."));
        return convertToDTO(customPermission);
    }

    public List<CustomPermissionDTO> getAllPermissions() {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "VIEW_CUSTOM_PERMISSIONS")) {
            throw new SecurityException("Нет права на просмотр кастомных прав");
        }

        return customPermissionRepository.getAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public void deletePermission(String code) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "DELETE_CUSTOM_PERMISSION")) {
            throw new SecurityException("Нет права на удаление кастомного права");
        }

        CustomPermission customPermission = customPermissionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Право с кодом " + code + " не существует."));

        if (!customPermission.getCreatePermission().getCompany().equals(user.getCompany())) {
            throw new SecurityException("Нельзя удалить право другой компании");
        }

        customPermissionRepository.delete(customPermission);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private CustomPermissionDTO convertToDTO(CustomPermission customPermission) {
        CustomPermissionDTO customPermissionDTO = new CustomPermissionDTO();
        customPermissionDTO.setId(customPermission.getId());
        customPermissionDTO.setName(customPermission.getName());
        customPermissionDTO.setCode(customPermission.getCode());
        customPermissionDTO.setDescription(customPermission.getDescription());
        return customPermissionDTO;
    }
}
