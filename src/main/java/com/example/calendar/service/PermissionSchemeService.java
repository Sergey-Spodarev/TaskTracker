package com.example.calendar.service;

import com.example.calendar.DTO.PermissionSchemeDTO;
import com.example.calendar.model.Company;
import com.example.calendar.model.PermissionScheme;
import com.example.calendar.model.RoleLevel;
import com.example.calendar.model.User;
import com.example.calendar.repository.PermissionSchemeRepository;
import com.example.calendar.repository.RoleLevelRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PermissionSchemeService {
    private final PermissionSchemeRepository permissionSchemeRepository;
    private final PermissionCheckService permissionCheckService;
    private final RoleLevelRepository roleLevelRepository;

    public PermissionSchemeService(PermissionSchemeRepository permissionSchemeRepository,
                                   PermissionCheckService permissionCheckService,
                                   RoleLevelRepository roleLevelRepository) {
        this.permissionSchemeRepository = permissionSchemeRepository;
        this.permissionCheckService = permissionCheckService;
        this.roleLevelRepository = roleLevelRepository;
    }

    public PermissionSchemeDTO addPermissionScheme(PermissionSchemeDTO dto) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "CREATE_PERMISSION_SCHEME")) {
            throw new SecurityException("Недостаточно прав для создания схемы разрешений");
        }

        PermissionScheme permissionScheme = new PermissionScheme();



        permissionScheme.setName(dto.getName());
        permissionScheme.setDescription(dto.getDescription());
        permissionScheme.setCreator(user);
        permissionScheme.setCompany(user.getCompany());
        permissionScheme.setCreatedAt(LocalDateTime.now());

        return convertToDTO(permissionSchemeRepository.save(permissionScheme));
    }

    public List<PermissionSchemeDTO> getAllPermissionSchemes() {
        User user = getCurrentUser();

        // ✅ Проверка прав
        if (!permissionCheckService.hasPermission(user, "VIEW_PERMISSION_SCHEMES")) {
            throw new SecurityException("Недостаточно прав для просмотра схем разрешений");
        }

        return permissionSchemeRepository.findByCompany(user.getCompany()).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public PermissionSchemeDTO getPermissionScheme(Long id) {
        User user = getCurrentUser();

        // ✅ Проверка прав
        if (!permissionCheckService.hasPermission(user, "VIEW_PERMISSION_SCHEMES")) {
            throw new SecurityException("Недостаточно прав для просмотра схемы разрешений");
        }

        PermissionScheme permissionScheme = permissionSchemeRepository
                .findByCompanyAndId(user.getCompany(), id)
                .orElseThrow(() -> new RuntimeException("Схема разрешений не найдена"));

        return convertToDTO(permissionScheme);
    }

    public PermissionSchemeDTO updatePermissionScheme(Long schemeId, PermissionSchemeDTO dto) {
        User user = getCurrentUser();

        // ✅ Проверка прав
        if (!permissionCheckService.hasPermission(user, "EDIT_PERMISSION_SCHEME")) {
            throw new SecurityException("Недостаточно прав для обновления схемы разрешений");
        }

        PermissionScheme permissionScheme = permissionSchemeRepository
                .findByCompanyAndId(user.getCompany(), schemeId)
                .orElseThrow(() -> new RuntimeException("Схема разрешений не найдена"));

        permissionScheme.setName(dto.getName());
        permissionScheme.setDescription(dto.getDescription());

        return convertToDTO(permissionSchemeRepository.save(permissionScheme));
    }

    public void deletePermissionScheme(Long id) {
        User user = getCurrentUser();

        // ✅ Проверка прав
        if (!permissionCheckService.hasPermission(user, "DELETE_PERMISSION_SCHEME")) {
            throw new SecurityException("Недостаточно прав для удаления схемы разрешений");
        }

        PermissionScheme permissionScheme = permissionSchemeRepository
                .findByCompanyAndId(user.getCompany(), id)
                .orElseThrow(() -> new RuntimeException("Схема разрешений не найдена"));

        permissionSchemeRepository.delete(permissionScheme);
    }

    /*public List<PermissionSchemeDTO> getSchemesByRoleLevel(Long roleLevelId) {
        User user = getCurrentUser();

        // ✅ Проверка прав
        if (!permissionCheckService.hasPermission(user, "VIEW_PERMISSION_SCHEMES")) {
            throw new SecurityException("Недостаточно прав для просмотра схем");
        }

        RoleLevel roleLevel = roleLevelRepository.findById(roleLevelId)
                .orElseThrow(() -> new RuntimeException("Уровень роли не найден"));

        if (!roleLevel.getRole().getDepartment().getCompany().equals(user.getCompany())) {
            throw new SecurityException("Уровень роли не принадлежит вашей компании");
        }

        return permissionSchemeRepository.findByRoleLevelAndCompany(roleLevel, user.getCompany()).stream()
                .map(this::convertToDTO)
                .toList();
    }*/

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private PermissionSchemeDTO convertToDTO(PermissionScheme permissionScheme) {
        PermissionSchemeDTO dto = new PermissionSchemeDTO();
        dto.setId(permissionScheme.getId());
        dto.setName(permissionScheme.getName());
        dto.setDescription(permissionScheme.getDescription());
        dto.setCreatedAt(permissionScheme.getCreatedAt());

        if (permissionScheme.getCreator() != null) {
            dto.setCreatorId(permissionScheme.getCreator().getId());
            dto.setCreatorName(permissionScheme.getCreator().getUserName());
        }

        return dto;
    }
}
