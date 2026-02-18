package com.example.calendar.service;

import com.example.calendar.DTO.SchemePermissionDTO;
import com.example.calendar.model.*;
import com.example.calendar.repository.PermissionSchemeRepository;
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
    private final PermissionSchemeRepository permissionSchemeRepository;
    public SchemePermissionService(SchemePermissionRepository schemePermissionRepository, PermissionSchemeRepository permissionSchemeRepository) {
        this.schemePermissionRepository = schemePermissionRepository;
        this.permissionSchemeRepository = permissionSchemeRepository;
    }

    public SchemePermissionDTO createSchemePermission(Long schemeId, SchemePermissionDTO schemePermissionDTO) {
        User user = getCurrentUser();//делать проверку может или как
        PermissionScheme permissionScheme = permissionSchemeRepository.findById(schemeId)
                .orElseThrow(() -> new RuntimeException("Схема не найдена"));
        if (!hasPermission(user, "create_scheme_permission")) {
            throw new SecurityException("Вы не можете сделать новое правило");
        }
        SchemePermission newSchemePermission = getSchemePermission(schemePermissionDTO);
        newSchemePermission.setScheme(permissionScheme);
        return convertToDTO(schemePermissionRepository.save(newSchemePermission));
    }

    public List<SchemePermissionDTO> getSchemePermissions(Long schemeId) {
        User user = getCurrentUser();//делать проверку может или как
        if (!hasPermission(user, "get_scheme_permission")) {
            throw new SecurityException("Вы не можете получить схему, ибо у вас не достаточный уровень доступа");
        }
        PermissionScheme permissionScheme = permissionSchemeRepository.findById(schemeId)
                .orElseThrow(() -> new RuntimeException("Схема не найдена"));
        List<SchemePermission> allSchemePermission = schemePermissionRepository.findByScheme(permissionScheme);
        return allSchemePermission.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public SchemePermissionDTO getSchemePermission(Long schemeId, Long permissionId){
        User user = getCurrentUser();
        if (!hasPermission(user, "get_scheme_permission")) {
            throw new SecurityException("Вы не можете получить схему, ибо у вас не достаточный уровень доступа");
        }
        PermissionScheme searchPermissionScheme = permissionSchemeRepository.findByCompanyAndId(user.getCompany(), permissionId)
                .orElseThrow(() -> new RuntimeException("Схема не найдена"));
        SchemePermission schemePermission = schemePermissionRepository.findByIdAndScheme(schemeId, searchPermissionScheme)
                .orElseThrow(() -> new RuntimeException("Ничего не найдено"));
        return convertToDTO(schemePermission);
    }

    public SchemePermissionDTO updateSchemePermission(Long schemeId, Long permissionId, SchemePermissionDTO schemePermissionDTO) {
        User user = getCurrentUser();
        if (!hasPermission(user, "update_scheme_permission")) {
            throw new SecurityException("Вы не можете обновить схему, ибо у вас не достаточный уровень доступа");
        }
        PermissionScheme searchPermissionScheme = permissionSchemeRepository.findByCompanyAndId(user.getCompany(), permissionId)
                .orElseThrow(() -> new RuntimeException("Схема не найдена"));
        SchemePermission schemePermission = schemePermissionRepository.findByIdAndScheme(schemeId, searchPermissionScheme)
                .orElseThrow(() -> new RuntimeException("Ничего не найдено"));

        schemePermission.setMaxLevel(schemePermissionDTO.getMaxLevel());
        schemePermission.setMinLevel(schemePermissionDTO.getMinLevel());
        schemePermission.setDescription(schemePermissionDTO.getDescription());
        return convertToDTO(schemePermissionRepository.save(schemePermission));
    }

    public void deleteSchemePermission(Long schemeId, Long permissionId) {
        User user = getCurrentUser();
        if (!hasPermission(user, "delete_scheme_permission")) {
            throw new SecurityException("Вы не можете удалить схему, ибо у вас не достаточный уровень доступа");
        }
        PermissionScheme searchPermissionScheme = permissionSchemeRepository.findByCompanyAndId(user.getCompany(), permissionId)
                .orElseThrow(() -> new RuntimeException("Схема не найдена"));
        SchemePermission schemePermission = schemePermissionRepository.findByIdAndScheme(schemeId, searchPermissionScheme)
                .orElseThrow(() -> new RuntimeException("Ничего не найдено"));
        schemePermissionRepository.delete(schemePermission);
    }

    private static SchemePermission getSchemePermission(SchemePermissionDTO schemePermissionDTO) {
        SchemePermission newSchemePermission = new SchemePermission();
        //BeanUtils.copyProperties(schemePermissionDTO, newSchemePermission); прикольная вещь которая копирует все поля, может надо будет где использовать
        newSchemePermission.setPermissionKey(schemePermissionDTO.getPermissionKey());
        newSchemePermission.setDescription(schemePermissionDTO.getDescription());
        newSchemePermission.setMinLevel(schemePermissionDTO.getMinLevel());
        newSchemePermission.setMaxLevel(schemePermissionDTO.getMaxLevel());
        return newSchemePermission;
    }

    public boolean hasPermission(User user, String action) {
        if (user.getSystemRole().equals(SystemRole.COMPANY_OWNER) ||
                user.getSystemRole().equals(SystemRole.DEPARTMENT_HEAD)) {
            return true;
        }

        RoleLevel userRoleLevel = user.getRoleLevel();
        Integer userLevel = userRoleLevel.getLevel();

        PermissionScheme permissionScheme = permissionSchemeRepository.findByRoleLevel(userRoleLevel)
                .orElseThrow(() -> new RuntimeException("Схема разрешений не найдена для уровня: " + userRoleLevel.getLevelName()));

        SchemePermission schemePermission = schemePermissionRepository.findBySchemeAndPermissionKey(permissionScheme, action)
                .orElseThrow(() -> new RuntimeException("Действие '" + action + "' запрещено для уровня: " + userRoleLevel.getLevelName()));



        return userLevel >= schemePermission.getMinLevel() && userLevel <= schemePermission.getMaxLevel();
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
