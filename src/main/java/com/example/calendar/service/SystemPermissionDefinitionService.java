package com.example.calendar.service;

import com.example.calendar.DTO.SystemPermissionDefinitionDTO;
import com.example.calendar.DTO.SystemPermissionDefinitionUpdateDTO;
import com.example.calendar.model.*;
import com.example.calendar.repository.SystemPermissionDefinitionRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SystemPermissionDefinitionService {
    private final SystemPermissionDefinitionRepository permissionDefinitionRepository;
    private final SystemPermissionCatalogService systemPermissionCatalogService;
    private final PermissionCheckService permissionCheckService;
    private final SystemPermissionDefinitionRepository systemPermissionDefinitionRepository;

    public SystemPermissionDefinitionService(SystemPermissionDefinitionRepository permissionDefinitionRepository, SystemPermissionCatalogService systemPermissionCatalogService, PermissionCheckService permissionCheckService, SystemPermissionDefinitionRepository systemPermissionDefinitionRepository) {
        this.permissionDefinitionRepository = permissionDefinitionRepository;
        this.systemPermissionCatalogService = systemPermissionCatalogService;
        this.permissionCheckService = permissionCheckService;
        this.systemPermissionDefinitionRepository = systemPermissionDefinitionRepository;
    }

    public SystemPermissionDefinitionDTO createSystemPermissionDefinition(SystemPermissionDefinitionDTO permissionDefinition) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "CREATE_PERMISSION_DEFINITION")) {
            throw new SecurityException("Нет права на создание набора прав");
        }

        for (SystemPermissionCatalog permissionCatalog : permissionDefinition.getSystemPermissionCatalog()) {
            if (!systemPermissionCatalogService.existsSystemPermissionCatalogByKey(permissionCatalog.getKey())) {
                throw new RuntimeException("Задачи " + permissionCatalog.getKey() + " не существует");
            }
        }

        if (permissionDefinitionRepository.existsByCode(permissionDefinition.getCode())) {
            throw new RuntimeException("Набор с кодом  " + permissionDefinition.getCode() + " уже существует");
        }

        SystemPermissionDefinition systemPermissionDefinition = new SystemPermissionDefinition();
        systemPermissionDefinition.setName(permissionDefinition.getName());
        systemPermissionDefinition.setDescription(permissionDefinition.getDescription());
        systemPermissionDefinition.setCode(permissionDefinition.getCode());
        systemPermissionDefinition.setSystemPermissionCatalog(permissionDefinition.getSystemPermissionCatalog());
        permissionDefinitionRepository.save(systemPermissionDefinition);
        return convertToDTO(systemPermissionDefinition);
    }

    public SystemPermissionDefinition createSystemPermissionDefinition(List<String> permissionCodes) {
        SystemPermissionDefinition systemPermissionDefinition = new SystemPermissionDefinition();
        Set<SystemPermissionCatalog> systemPermissionCatalogs = new HashSet<>();
        String name = String.join("_", permissionCodes);
        String code = String.join("_", permissionCodes);
        String description = "Набор системных прав: " + name;
        for (String permissionCode : permissionCodes) {
            SystemPermissionCatalog catalog = systemPermissionCatalogService.getByKey(SystemPermissionKey.valueOf(permissionCode));
            systemPermissionCatalogs.add(catalog);
        }
        systemPermissionDefinition.setSystemPermissionCatalog(systemPermissionCatalogs);
        systemPermissionDefinition.setName(name);
        systemPermissionDefinition.setDescription(description);
        systemPermissionDefinition.setCode(code);
        systemPermissionDefinitionRepository.save(systemPermissionDefinition);
        return systemPermissionDefinition;
    }

    public void updateSystemPermissionDefinition(SystemPermissionDefinition systemPermissionDefinition, List<String> permissionCodes) {
        Set<SystemPermissionCatalog> currentCatalogs = systemPermissionDefinition.getSystemPermissionCatalog();
        Set<String> currentKeys = currentCatalogs.stream()
                .map(c -> c.getKey().name())
                .collect(Collectors.toSet());

        for (String permissionCode : permissionCodes) {
            if (!currentKeys.contains(permissionCode)) {
                SystemPermissionCatalog catalog = systemPermissionCatalogService.getByKey(SystemPermissionKey.valueOf(permissionCode));
                currentCatalogs.add(catalog);
            }
        }

        systemPermissionDefinitionRepository.save(systemPermissionDefinition);
    }

    public SystemPermissionDefinitionDTO updateSystemPermissionDefinition(SystemPermissionDefinitionDTO permissionDefinition) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "EDIT_PERMISSION_DEFINITION")) {
            throw new SecurityException("Нет права на редактирование набора прав");
        }

        SystemPermissionDefinition systemPermissionDefinition = permissionDefinitionRepository.findByCode(permissionDefinition.getCode())
                .orElseThrow(() -> new RuntimeException("Роли с таким именем " + permissionDefinition.getName() + " не существует"));

        systemPermissionDefinition.setName(permissionDefinition.getName());
        systemPermissionDefinition.setDescription(permissionDefinition.getDescription());
        systemPermissionDefinition.setSystemPermissionCatalog(permissionDefinition.getSystemPermissionCatalog());
        permissionDefinitionRepository.save(systemPermissionDefinition);
        return convertToDTO(systemPermissionDefinition);
    }

    public SystemPermissionDefinitionDTO addSystemPermissionDefinition(SystemPermissionDefinitionUpdateDTO updateData) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "EDIT_PERMISSION_DEFINITION")) {
            throw new SecurityException("Нет права на редактирование набора прав");
        }

        SystemPermissionDefinition systemPermissionDefinition = permissionDefinitionRepository.findByCode(updateData.getDefinitionCode())
                .orElseThrow(() -> new RuntimeException("Набора задач " + updateData.getDefinitionCode() + " на данный момент не существует"));

        SystemPermissionCatalog systemPermissionCatalog = systemPermissionCatalogService.getByKey(updateData.getPermissionKey());

        systemPermissionDefinition.getSystemPermissionCatalog().add(systemPermissionCatalog);
        permissionDefinitionRepository.save(systemPermissionDefinition);
        return convertToDTO(systemPermissionDefinition);
    }

    public boolean existsSystemPermissionDefinitionByCode(String code) {
        SystemPermissionKey key = SystemPermissionKey.valueOf(code);
        return systemPermissionCatalogService.existsSystemPermissionCatalogByKey(key);
    }

    public SystemPermissionDefinitionDTO getByCode(String code) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "VIEW_PERMISSION_DEFINITIONS")) {
            throw new SecurityException("Нет права на просмотр наборов прав");
        }

        SystemPermissionDefinition searchPermissionDefinition = permissionDefinitionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Данной задачи " + code + " не существует"));
        return convertToDTO(searchPermissionDefinition);
    }

    public List<SystemPermissionDefinitionDTO> getAll() {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "VIEW_PERMISSION_DEFINITIONS")) {
            throw new SecurityException("Нет права на просмотр наборов прав");
        }

        return permissionDefinitionRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<String> getSystemPermissionCurrentUser(SystemPermissionDefinition systemPermissionDefinition) {
        List<String> permission = new ArrayList<>();
        if (systemPermissionDefinition != null) {
            systemPermissionDefinition.getSystemPermissionCatalog()
                    .forEach(catalog -> permission.add(catalog.getKey().name()));
        }
        return permission;
    }

    public void deleteByCode(String code) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "DELETE_PERMISSION_DEFINITION")) {
            throw new SecurityException("Нет права на удаление набора прав");
        }

        SystemPermissionDefinition systemPermissionDefinition = permissionDefinitionRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Данной задачи " + code + " не существует"));
        permissionDefinitionRepository.delete(systemPermissionDefinition);
    }

    public SystemPermissionDefinitionDTO deleteSystemPermissionDefinition(SystemPermissionDefinitionUpdateDTO deleteData) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "EDIT_PERMISSION_DEFINITION")) {
            throw new SecurityException("Нет права на редактирование набора прав");
        }

        SystemPermissionDefinition systemPermissionDefinition = permissionDefinitionRepository.findByCode(deleteData.getDefinitionCode())
                .orElseThrow(() -> new RuntimeException("Набора задач " + deleteData.getDefinitionCode() + " на данный момент не существует"));

        SystemPermissionCatalog systemPermissionCatalog = systemPermissionCatalogService.getByKey(deleteData.getPermissionKey());

        systemPermissionDefinition.getSystemPermissionCatalog().remove(systemPermissionCatalog);
        permissionDefinitionRepository.save(systemPermissionDefinition);
        return convertToDTO(systemPermissionDefinition);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser();
    }

    private SystemPermissionDefinitionDTO convertToDTO(SystemPermissionDefinition systemPermissionDefinition) {
        SystemPermissionDefinitionDTO systemPermissionDefinitionDTO = new SystemPermissionDefinitionDTO();
        systemPermissionDefinitionDTO.setId(systemPermissionDefinition.getId());
        systemPermissionDefinitionDTO.setName(systemPermissionDefinition.getName());
        systemPermissionDefinitionDTO.setDescription(systemPermissionDefinition.getDescription());
        systemPermissionDefinitionDTO.setCode(systemPermissionDefinition.getCode());
        systemPermissionDefinitionDTO.setSystemPermissionCatalog(systemPermissionDefinition.getSystemPermissionCatalog());
        return systemPermissionDefinitionDTO;
    }
}
