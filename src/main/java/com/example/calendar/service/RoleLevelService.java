package com.example.calendar.service;

import com.example.calendar.DTO.CreateRoleLevelDTO;
import com.example.calendar.DTO.RoleLevelDTO;
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

import java.util.List;

@Service
@Transactional
public class RoleLevelService {
    private final RoleLevelRepository roleLevelRepository;
    private final SchemePermissionService schemePermissionService;
    private final RoleRepository roleRepository; // ← Добавляем

    public RoleLevelService(RoleLevelRepository roleLevelRepository,
                            SchemePermissionService schemePermissionService,
                            RoleRepository roleRepository) {
        this.roleLevelRepository = roleLevelRepository;
        this.schemePermissionService = schemePermissionService;
        this.roleRepository = roleRepository;
    }

    public RoleLevelDTO create(CreateRoleLevelDTO roleLevelDTO) {
        User user = getCurrentUser();

        // ФИКС: инвертируем условие
        if (!schemePermissionService.hasPermission(user, "create_role_level")) {
            throw new SecurityException("Недостаточно прав для создания уровня роли");
        }

        // Получаем роль из DTO
        Role role = roleRepository.findById(roleLevelDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Роль не найдена"));

        // Проверяем, что роль принадлежит компании пользователя
        if (!role.getDepartment().getCompany().equals(user.getCompany())) {
            throw new SecurityException("Роль не принадлежит вашей компании");
        }

        // Проверяем, нет ли уже уровня с таким номером для этой роли
        boolean levelExists = roleLevelRepository.findByRoleAndCompany(role, user.getCompany())
                .map(rl -> rl.getLevel().equals(roleLevelDTO.getLevel()))
                .orElse(false);

        if (levelExists) {
            throw new RuntimeException("Уровень с номером " + roleLevelDTO.getLevel() + " уже существует для этой роли");
        }

        RoleLevel roleLevel = new RoleLevel();
        roleLevel.setLevel(roleLevelDTO.getLevel());
        roleLevel.setLevelName(roleLevelDTO.getLevelName());
        roleLevel.setRole(role); // ← ФИКС: устанавливаем роль

        return convertToRoleLevelDTO(roleLevelRepository.save(roleLevel));
    }

    public RoleLevelDTO getById(Long id) {
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "get_role_level")) {
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

        if (!schemePermissionService.hasPermission(user, "get_role_level")) {
            throw new SecurityException("Недостаточно прав для просмотра уровней ролей");
        }

        List<RoleLevel> roleLevels = roleLevelRepository.findAllByCompany(user.getCompany());
        return roleLevels.stream()
                .map(this::convertToRoleLevelDTO)
                .toList();
    }

    public RoleLevelDTO update(RoleLevelDTO roleLevelDTO) {
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "update_role_level")) {
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

    public void delete(Long id) {
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "delete_role_level")) {
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