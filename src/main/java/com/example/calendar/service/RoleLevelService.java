package com.example.calendar.service;

import com.example.calendar.DTO.CreateRoleLevelDTO;
import com.example.calendar.DTO.RoleLevelDTO;
import com.example.calendar.model.PermissionScheme;
import com.example.calendar.model.Role;
import com.example.calendar.model.RoleLevel;
import com.example.calendar.model.User;
import com.example.calendar.repository.RoleLevelRepository;
import com.example.calendar.repository.RoleRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoleLevelService {
    private final RoleLevelRepository roleLevelRepository;
    private final PermissionCheckService permissionCheckService;
    private final RoleRepository roleRepository;
    private final PermissionSchemeService permissionSchemeService;

    public RoleLevelService(RoleLevelRepository roleLevelRepository,
                            PermissionCheckService permissionCheckService,
                            RoleRepository roleRepository,
                            PermissionSchemeService permissionSchemeService) {
        this.roleLevelRepository = roleLevelRepository;
        this.permissionCheckService = permissionCheckService;
        this.roleRepository = roleRepository;
        this.permissionSchemeService = permissionSchemeService;
    }

    public RoleLevelDTO create(CreateRoleLevelDTO roleLevelDTO) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "CREATE_ROLE_LEVEL")) {
            throw new SecurityException("Недостаточно прав для создания уровня роли");
        }

        // Проверяем, что roleCode не null
        if (roleLevelDTO.getRoleCode() == null) {
            throw new IllegalArgumentException("Code роли не может быть null. Получено: " + roleLevelDTO);
        }

        // Получаем роль из DTO
        Role role = roleRepository.findByCode(roleLevelDTO.getRoleCode())
                .orElseThrow(() -> new RuntimeException("Роль не найдена с Code: " + roleLevelDTO.getRoleCode()));

        // Проверяем, что роль принадлежит компании пользователя
        if (role.getDepartment() == null) {
            throw new SecurityException("У роли не назначен департамент. Сначала назначьте департамент для роли.");
        }

        // Сравниваем компании
        Long roleCompanyId = role.getDepartment().getCompany().getId();
        Long userCompanyId = user.getCompany().getId();

        if (!role.getDepartment().getCompany().getId().equals(user.getCompany().getId())) {
            throw new SecurityException(String.format(
                    "Роль не принадлежит вашей компании. Компания роли: %s (ID: %d), Компания пользователя: %s (ID: %d)",
                    role.getDepartment().getCompany().getName(),
                    roleCompanyId,
                    user.getCompany().getName(),
                    userCompanyId
            ));
        }

        // Проверяем, нет ли уже уровня для этой роли
        Optional<RoleLevel> existingLevel = roleLevelRepository.findByRole(role);
        if (existingLevel.isPresent()) {
            throw new RuntimeException("Уровень для роли " + role.getCode() + " уже существует");
        }

        RoleLevel roleLevel = new RoleLevel();
        roleLevel.setLevel(roleLevelDTO.getLevel());
        roleLevel.setLevelName(roleLevelDTO.getLevelName());
        roleLevel.setRole(role);
        roleLevel.setPermissionScheme(null);

        RoleLevel saved = roleLevelRepository.save(roleLevel);

        return convertToRoleLevelDTO(saved);
    }

    public void updateRoleLevelPermissions(Long roleLevelId, List<String> permissions) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "EDIT_ROLE_LEVEL")) {
            throw new SecurityException("Недостаточно прав для обновления уровня роли");
        }

        RoleLevel roleLevel = roleLevelRepository.findById(roleLevelId)
                .orElseThrow(() -> new RuntimeException("Роли с ID = " + roleLevelId + " не найдено"));

        if (roleLevel.getPermissionScheme() == null) {
            PermissionScheme permissionScheme = permissionSchemeService.createPermissionScheme(permissions);
            roleLevel.setPermissionScheme(permissionScheme);
            roleLevelRepository.save(roleLevel);
        } else {
            permissionSchemeService.updatePermissionScheme(roleLevel.getPermissionScheme(), permissions);
        }
    }

    public RoleLevelDTO updateRoleLevel(Long levelId, RoleLevelDTO roleLevelDTO) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "EDIT_ROLE_LEVEL")) {
            throw new SecurityException("Недостаточно прав для обновления уровня роли");
        }

        RoleLevel updateRoleLevel = roleLevelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Роль не существует с таким ID: " + levelId));

        updateRoleLevel.setLevel(roleLevelDTO.getLevel());
        updateRoleLevel.setLevelName(roleLevelDTO.getLevelName());
        updateRoleLevel.setRole(updateRoleLevel.getRole());
        return convertToRoleLevelDTO(roleLevelRepository.save(updateRoleLevel));
    }

    public RoleLevelDTO getById(Long id) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "VIEW_ROLE_LEVELS")) {
            throw new SecurityException("Недостаточно прав для просмотра уровня роли");
        }

        RoleLevel roleLevel = roleLevelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Уровень роли не найден"));

        // Проверяем принадлежность компании
        if (!roleLevel.getRole().getDepartment().getCompany().equals(user.getCompany())) {
            throw new SecurityException("Уровень роли не принадлежит вашей компании");
        }

        return convertToRoleLevelDTO(roleLevel);
    }

    public List<RoleLevelDTO> getAll() {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "VIEW_ROLE_LEVELS")) {
            throw new SecurityException("Недостаточно прав для просмотра уровней ролей");
        }

        List<RoleLevel> roleLevels = roleLevelRepository.findAllByCompany(user.getCompany());
        return roleLevels.stream()
                .map(this::convertToRoleLevelDTO)
                .toList();
    }

    public RoleLevelDTO update(RoleLevelDTO roleLevelDTO) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "EDIT_ROLE_LEVEL")) {
            throw new SecurityException("Недостаточно прав для обновления уровня роли");
        }

        RoleLevel roleLevel = roleLevelRepository.findById(roleLevelDTO.getId())
                .orElseThrow(() -> new RuntimeException("Уровень роли не найден"));

        // Проверяем принадлежность компании
        if (!roleLevel.getRole().getDepartment().getCompany().equals(user.getCompany())) {
            throw new SecurityException("Уровень роли не принадлежит вашей компании");
        }

        // Проверяем, не меняется ли уровень на уже существующий (если изменился)
        if (!roleLevel.getLevel().equals(roleLevelDTO.getLevel())) {
            boolean levelExists = roleLevelRepository.findByRoleAndCompany(roleLevel.getRole(), user.getCompany())
                    .map(rl -> rl.getLevel().equals(roleLevelDTO.getLevel()))
                    .orElse(false);

            if (levelExists) {
                throw new RuntimeException("Уровень с номером " + roleLevelDTO.getLevel() + " уже существует для этой роли");
            }
        }

        roleLevel.setLevel(roleLevelDTO.getLevel());
        roleLevel.setLevelName(roleLevelDTO.getLevelName());
        // Роль не меняем при обновлении

        return convertToRoleLevelDTO(roleLevelRepository.save(roleLevel));
    }

    public List<RoleLevelDTO> getRoleLevelsByRole(String roleCode) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "VIEW_ROLE_LEVELS")) {
            throw new SecurityException("Недостаточно прав для просмотра уровня роли");
        }

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("<UNK> <UNK> <UNK>"));

        List<RoleLevel> roleLevels = roleLevelRepository.findAllByRole(role);
        return roleLevels.stream()
                .map(this::convertToRoleLevelDTO)
                .toList();
    }

    public List<String> getRoleLevelPermissions(Long roleLevelId) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "VIEW_ROLE_LEVELS")) {
            throw new SecurityException("Недостаточно прав для просмотра уровня роли");
        }

        RoleLevel roleLevel = roleLevelRepository.findById(roleLevelId)
                .orElseThrow(() -> new RuntimeException("Такой роли нет"));

        return permissionSchemeService.getPermissionCurrentUser(roleLevel.getPermissionScheme());
    }

    public void delete(Long id) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "DELETE_ROLE_LEVEL")) {
            throw new SecurityException("Недостаточно прав для удаления уровня роли");
        }

        RoleLevel roleLevel = roleLevelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Уровень роли не найден"));

        // Проверяем принадлежность компании
        if (!roleLevel.getRole().getDepartment().getCompany().equals(user.getCompany())) {
            throw new SecurityException("Нельзя удалить уровень роли из чужой компании");
        }

        // Проверяем, не используется ли где-то (например, в PermissionScheme)
        // TODO: Добавить проверку зависимостей

        roleLevelRepository.deleteById(id);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private RoleLevelDTO convertToRoleLevelDTO(RoleLevel roleLevel) {
        RoleLevelDTO roleLevelDTO = new RoleLevelDTO();
        roleLevelDTO.setId(roleLevel.getId());
        roleLevelDTO.setLevel(roleLevel.getLevel());
        roleLevelDTO.setLevelName(roleLevel.getLevelName());
        return roleLevelDTO;
    }
}