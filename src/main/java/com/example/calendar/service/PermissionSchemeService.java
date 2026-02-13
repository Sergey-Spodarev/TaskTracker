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
    private final SchemePermissionService schemePermissionService; // ← Добавил для проверки прав
    private final RoleLevelRepository roleLevelRepository; // ← Для установки RoleLevel

    public PermissionSchemeService(PermissionSchemeRepository permissionSchemeRepository,
                                   SchemePermissionService schemePermissionService,
                                   RoleLevelRepository roleLevelRepository) {
        this.permissionSchemeRepository = permissionSchemeRepository;
        this.schemePermissionService = schemePermissionService;
        this.roleLevelRepository = roleLevelRepository;
    }

    public PermissionSchemeDTO addPermissionScheme(PermissionSchemeDTO permissionSchemeDTO) {
        User user = getCurrentUser();

        // Проверка прав
        if (!schemePermissionService.hasPermission(user, "create_permission_scheme")) {
            throw new SecurityException("Недостаточно прав для создания схемы разрешений");
        }

        PermissionScheme permissionScheme = new PermissionScheme();

        // Устанавливаем RoleLevel из DTO
        if (permissionSchemeDTO.getRoleLevelId() != null) {
            RoleLevel roleLevel = roleLevelRepository.findById(permissionSchemeDTO.getRoleLevelId())
                    .orElseThrow(() -> new RuntimeException("Уровень роли не найден"));

            // Проверяем, что RoleLevel принадлежит компании пользователя
            if (!roleLevel.getRole().getDepartment().getCompany().equals(user.getCompany())) {
                throw new SecurityException("Уровень роли не принадлежит вашей компании");
            }
            permissionScheme.setRoleLevel(roleLevel);
        }

        permissionScheme.setName(permissionSchemeDTO.getName());
        permissionScheme.setDescription(permissionSchemeDTO.getDescription());
        permissionScheme.setCreator(user);
        permissionScheme.setCompany(user.getCompany());
        permissionScheme.setCreatedAt(LocalDateTime.now());

        return convertToDTO(permissionSchemeRepository.save(permissionScheme));
    }

    public List<PermissionSchemeDTO> getAllPermissionSchemes() {
        User user = getCurrentUser();

        // Проверка прав
        if (!schemePermissionService.hasPermission(user, "view_permission_schemes")) {
            throw new SecurityException("Недостаточно прав для просмотра схем разрешений");
        }

        Company company = user.getCompany();
        List<PermissionScheme> allPermissionSchemes = permissionSchemeRepository.findByCompany(company);
        return allPermissionSchemes.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public PermissionSchemeDTO getPermissionScheme(Long id) {
        User user = getCurrentUser();

        // Проверка прав
        if (!schemePermissionService.hasPermission(user, "view_permission_schemes")) {
            throw new SecurityException("Недостаточно прав для просмотра схемы разрешений");
        }

        Company company = user.getCompany();
        PermissionScheme permissionScheme = permissionSchemeRepository.findByCompanyAndId(company, id)
                .orElseThrow(() -> new RuntimeException("Схема разрешений не найдена"));
        return convertToDTO(permissionScheme);
    }

    public PermissionSchemeDTO updatePermissionScheme(Long schemeId, PermissionSchemeDTO permissionSchemeDTO) {
        User user = getCurrentUser();

        // Проверка прав
        if (!schemePermissionService.hasPermission(user, "update_permission_scheme")) {
            throw new SecurityException("Недостаточно прав для обновления схемы разрешений");
        }

        PermissionScheme permissionScheme = permissionSchemeRepository.findByCompanyAndId(user.getCompany(), schemeId)
                .orElseThrow(() -> new RuntimeException("Схема разрешений не найдена"));

        // Обновляем RoleLevel если нужно
        if (permissionSchemeDTO.getRoleLevelId() != null &&
                !permissionSchemeDTO.getRoleLevelId().equals(permissionScheme.getRoleLevel().getId())) {

            RoleLevel newRoleLevel = roleLevelRepository.findById(permissionSchemeDTO.getRoleLevelId())
                    .orElseThrow(() -> new RuntimeException("Уровень роли не найден"));

            // Проверяем принадлежность компании
            if (!newRoleLevel.getRole().getDepartment().getCompany().equals(user.getCompany())) {
                throw new SecurityException("Уровень роли не принадлежит вашей компании");
            }
            permissionScheme.setRoleLevel(newRoleLevel);
        }

        permissionScheme.setName(permissionSchemeDTO.getName());
        permissionScheme.setDescription(permissionSchemeDTO.getDescription());
        // creator и company не меняем при обновлении
        // createdAt тоже не меняем - это дата создания

        return convertToDTO(permissionSchemeRepository.save(permissionScheme));
    }

    public void deletePermissionScheme(Long id) {
        User user = getCurrentUser();

        // Проверка прав
        if (!schemePermissionService.hasPermission(user, "delete_permission_scheme")) {
            throw new SecurityException("Недостаточно прав для удаления схемы разрешений");
        }

        Company company = user.getCompany();
        PermissionScheme permissionScheme = permissionSchemeRepository.findByCompanyAndId(company, id)
                .orElseThrow(() -> new RuntimeException("Схема разрешений не найдена"));
        permissionSchemeRepository.delete(permissionScheme);
    }

    // Дополнительный метод для поиска схем по уровню роли
    public List<PermissionSchemeDTO> getSchemesByRoleLevel(Long roleLevelId) {
        User user = getCurrentUser();
        RoleLevel roleLevel = roleLevelRepository.findById(roleLevelId)
                .orElseThrow(() -> new RuntimeException("Уровень роли не найден"));

        // Проверяем принадлежность компании
        if (!roleLevel.getRole().getDepartment().getCompany().equals(user.getCompany())) {
            throw new SecurityException("Уровень роли не принадлежит вашей компании");
        }

        List<PermissionScheme> schemes = permissionSchemeRepository.findByRoleLevelAndCompany(roleLevel, user.getCompany());
        return schemes.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private PermissionSchemeDTO convertToDTO(PermissionScheme permissionScheme) {
        PermissionSchemeDTO permissionSchemeDTO = new PermissionSchemeDTO();
        permissionSchemeDTO.setId(permissionScheme.getId());
        permissionSchemeDTO.setName(permissionScheme.getName());
        permissionSchemeDTO.setDescription(permissionScheme.getDescription());
        permissionSchemeDTO.setCreatedAt(permissionScheme.getCreatedAt());

        // Добавляем ID связанных сущностей
        if (permissionScheme.getRoleLevel() != null) {
            permissionSchemeDTO.setRoleLevelId(permissionScheme.getRoleLevel().getId());
            permissionSchemeDTO.setRoleLevelName(permissionScheme.getRoleLevel().getLevelName());
        }
        if (permissionScheme.getCreator() != null) {
            permissionSchemeDTO.setCreatorId(permissionScheme.getCreator().getId());
            permissionSchemeDTO.setCreatorName(permissionScheme.getCreator().getUserName());
        }

        return permissionSchemeDTO;
    }
}
