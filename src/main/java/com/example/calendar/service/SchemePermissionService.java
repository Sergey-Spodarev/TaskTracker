package com.example.calendar.service;

import com.example.calendar.DTO.SchemePermissionDTO;
import com.example.calendar.model.*;
import com.example.calendar.repository.SchemePermissionRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SchemePermissionService {
    private final SchemePermissionRepository schemePermissionRepository;
    private final PermissionCheckService permissionCheckService;
    public SchemePermissionService(SchemePermissionRepository schemePermissionRepository, PermissionCheckService permissionCheckService) {
        this.schemePermissionRepository = schemePermissionRepository;
        this.permissionCheckService = permissionCheckService;
    }

    public SchemePermissionDTO createSchemePermission(SchemePermissionDTO schemePermissionDTO) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "CREATE_SCHEME_PERMISSION")) {
            throw new SecurityException("Нет права на создание уровня доступа");
        }
        if (schemePermissionRepository.existsByPermissionKey(schemePermissionDTO.getPermissionKey())) {
            throw new RuntimeException("Правило с таким " + schemePermissionDTO.getPermissionKey() + " уже существует");
        }
        if (schemePermissionDTO.getMinLevel() < schemePermissionDTO.getMaxLevel()) {//у меня логика что самый высокий уровень доступа меньше значение так легче ибо не надо переделывать если появится новая роль
            throw new RuntimeException("Минимальный доступ меньше чем максимальный");
        }

        SchemePermission schemePermission = new SchemePermission();
        schemePermission.setPermissionKey(schemePermissionDTO.getPermissionKey());
        schemePermission.setDescription(schemePermissionDTO.getDescription());
        schemePermission.setMinLevel(schemePermissionDTO.getMinLevel());
        schemePermission.setMaxLevel(schemePermissionDTO.getMaxLevel());
        schemePermissionRepository.save(schemePermission);
        return schemePermissionDTO;
    }

    public SchemePermissionDTO updateSchemePermission(SchemePermissionDTO schemePermissionDTO) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "EDIT_SCHEME_PERMISSION")) {
            throw new SecurityException("Нет права на редактирование уровня доступа");
        }
        SchemePermission schemePermission = schemePermissionRepository.findByPermissionKey(schemePermissionDTO.getPermissionKey())
                .orElseThrow(() -> new RuntimeException("Задачи с таким кодом " + schemePermissionDTO.getPermissionKey() + " не существует"));

        schemePermission.setDescription(schemePermissionDTO.getDescription());
        schemePermission.setMinLevel(schemePermissionDTO.getMinLevel());
        schemePermission.setMaxLevel(schemePermissionDTO.getMaxLevel());
        return convertToDTO(schemePermissionRepository.save(schemePermission));
    }

    public SchemePermissionDTO getSchemePermissionByKey(String permissionKey){
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "VIEW_SCHEME_PERMISSIONS")) {
            throw new SecurityException("Нет права на просмотр уровней доступа");
        }
        SchemePermission schemePermission = schemePermissionRepository.findByPermissionKey(permissionKey)
                .orElseThrow(() -> new RuntimeException("Право с таким кодом " + permissionKey + " не существует"));
        return convertToDTO(schemePermission);
    }

    public SchemePermission getSchemePermissionByCode(String permissionKey){
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "VIEW_SCHEME_PERMISSIONS")) {
            throw new SecurityException("Нет права на просмотр уровней доступа");
        }
        return schemePermissionRepository.findByPermissionKey(permissionKey)
                .orElseThrow(() -> new RuntimeException("Право с таким кодом " + permissionKey + " не существует"));
    }

    public SchemePermissionDTO getSchemePermission(Long id) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "VIEW_SCHEME_PERMISSIONS")) {
            throw new SecurityException("Нет права на просмотр уровней доступа");
        }

        SchemePermission schemePermission = schemePermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задачи с id = " + id + " не существует"));
        return convertToDTO(schemePermission);
    }

    public List<SchemePermissionDTO> getAllSchemePermissions() {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "VIEW_SCHEME_PERMISSIONS")) {
            throw new SecurityException("Нет права на просмотр уровней доступа");
        }

        return schemePermissionRepository.getAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    /** может потом сделать
     * Получение уровней, подходящих под указанный уровень пользователя
     */

    public void deleteSchemePermission(String permissionKey) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "DELETE_SCHEME_PERMISSION")) {
            throw new SecurityException("Нет права на удаление уровня доступа");
        }

        SchemePermission schemePermission = schemePermissionRepository.findByPermissionKey(permissionKey)
                .orElseThrow(() -> new RuntimeException("Право с таким кодом " + permissionKey + " не существует"));

        schemePermissionRepository.delete(schemePermission);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private SchemePermissionDTO convertToDTO(SchemePermission schemePermission) {
        SchemePermissionDTO schemePermissionDTO = new SchemePermissionDTO();
        schemePermissionDTO.setId(schemePermission.getId());
        schemePermissionDTO.setPermissionKey(schemePermission.getPermissionKey());
        schemePermissionDTO.setDescription(schemePermission.getDescription());
        schemePermissionDTO.setMaxLevel(schemePermission.getMaxLevel());
        schemePermissionDTO.setMinLevel(schemePermission.getMinLevel());
        return schemePermissionDTO;
    }
}
