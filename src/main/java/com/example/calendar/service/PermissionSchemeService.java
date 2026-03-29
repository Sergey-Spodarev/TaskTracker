package com.example.calendar.service;

import com.example.calendar.DTO.PermissionSchemeDTO;
import com.example.calendar.model.*;
import com.example.calendar.repository.PermissionSchemeRepository;
import com.example.calendar.security.CustomUserDetails;
import com.example.calendar.security.CustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PermissionSchemeService {
    private final PermissionSchemeRepository permissionSchemeRepository;
    private final PermissionCheckService permissionCheckService;
    private final SystemPermissionDefinitionService systemPermissionDefinitionService;
    private final CustomPermissionService customPermissionService;

    public PermissionSchemeService(PermissionSchemeRepository permissionSchemeRepository,
                                   PermissionCheckService permissionCheckService,
                                   SystemPermissionDefinitionService systemPermissionDefinitionService,
                                   CustomPermissionService customPermissionService) {
        this.permissionSchemeRepository = permissionSchemeRepository;
        this.permissionCheckService = permissionCheckService;
        this.systemPermissionDefinitionService = systemPermissionDefinitionService;
        this.customPermissionService = customPermissionService;
    }

    public PermissionSchemeDTO createPermissionScheme(PermissionSchemeDTO dto) {
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

    public PermissionScheme createPermissionScheme(List<String> permissions) {
        User user = getCurrentUser();
        PermissionScheme permissionScheme = new PermissionScheme();
        List<String> systemPermissions = new ArrayList<>();
        List<String> customPermissions = new ArrayList<>();

        String name = String.join("_", permissions);
        String description = "Набор системных прав: " + name;

        for (String permission : permissions) {
            if (systemPermissionDefinitionService.existsSystemPermissionDefinitionByCode(permission)) {
                systemPermissions.add(permission);
            } else {
                customPermissions.add(permission);
            }
        }

        System.out.println("Сиситемные права = ");
        for (String systemPermission : systemPermissions) {
            System.out.println(" " + systemPermission);
        }
        System.out.println("Кастомные права = ");
        for (String customPermission : customPermissions) {
            System.out.println(" " + customPermission);
        }

        SystemPermissionDefinition systemPermissionDefinition = systemPermissionDefinitionService.createSystemPermissionDefinition(systemPermissions);
        permissionScheme.setSystemPermissionDefinition(systemPermissionDefinition);
        CustomPermission customPermission = customPermissionService.createCustomPermission(customPermissions);
        permissionScheme.setCustomPermission(customPermission);
        permissionScheme.setCreatedAt(LocalDateTime.now());
        permissionScheme.setCreator(user);
        permissionScheme.setCompany(user.getCompany());
        permissionScheme.setDescription(description);
        permissionScheme.setName(name);
        permissionSchemeRepository.save(permissionScheme);
        return permissionScheme;
    }

    public void updatePermissionScheme(PermissionScheme permissionScheme, List<String> permissions) {
        User user = getCurrentUser();

        List<String> systemPermissions = new ArrayList<>();
        List<String> customPermissions = new ArrayList<>();

        for (String permission : permissions) {
            if (systemPermissionDefinitionService.existsSystemPermissionDefinitionByCode(permission)) {
                systemPermissions.add(permission);
            } else {
                customPermissions.add(permission);
            }
        }

        System.out.println("Сиситемные права = ");
        for (String systemPermission : systemPermissions) {
            System.out.println(" " + systemPermission);
        }
        System.out.println("Кастомные права = ");
        for (String customPermission : customPermissions) {
            System.out.println(" " + customPermission);
        }

        systemPermissionDefinitionService.updateSystemPermissionDefinition(permissionScheme.getSystemPermissionDefinition(), systemPermissions);
        customPermissionService.updateCustomPermission(permissionScheme.getCustomPermission(), customPermissions);
        permissionSchemeRepository.save(permissionScheme);
    }

    @Transactional(readOnly = true)
    public Set<String> getUserPermissionSchemes() {
        User user = getCurrentUser();

        if (user.getSystemRole().equals(SystemRole.COMPANY_OWNER)) {
            return Arrays.stream(SystemPermissionKey.values())
                    .map(Enum::name)
                    .collect(Collectors.toSet());
        }

        // ✅ Загружаем PermissionScheme со всеми нужными коллекциями
        PermissionScheme userPermissionScheme = permissionSchemeRepository
                .findByIdWithPermissions(user.getRoleLevel().getPermissionScheme().getId())
                .orElseThrow(() -> new RuntimeException("Схема разрешений не найдена"));

        Set<String> result = new HashSet<>();

        // Теперь коллекции загружены
        for (SystemPermissionCatalog userSystemPermission : userPermissionScheme.getSystemPermissionDefinition().getSystemPermissionCatalog()) {
            result.add(userSystemPermission.getKey().name());
        }

        CustomPermission customPermission = userPermissionScheme.getCustomPermission();
        if (customPermission != null) {
            for (SchemePermission schemePermission : customPermission.getSchemePermission()) {
                result.add(schemePermission.getPermissionKey());
            }
        }

        return result;
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

    public List<String> getPermissionCurrentUser(PermissionScheme permissionScheme) {
        ArrayList<String> allPermissionsCurrentUser = new ArrayList<>();

        List<String> schemePermissionUser = systemPermissionDefinitionService.getSystemPermissionCurrentUser(permissionScheme.getSystemPermissionDefinition());
        allPermissionsCurrentUser.addAll(schemePermissionUser);
        List<String> customPermissionUser = customPermissionService.getCustomPermissionCurrentUser(permissionScheme.getCustomPermission());
        allPermissionsCurrentUser.addAll(customPermissionUser);
        return allPermissionsCurrentUser;
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
