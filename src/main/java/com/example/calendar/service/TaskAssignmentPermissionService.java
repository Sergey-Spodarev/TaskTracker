package com.example.calendar.service;

import com.example.calendar.DTO.DepartmentDTO;
import com.example.calendar.DTO.TaskAssignmentPermissionDTO;
import com.example.calendar.model.Department;
import com.example.calendar.model.RoleLevel;
import com.example.calendar.model.TaskAssignmentPermission;
import com.example.calendar.model.User;
import com.example.calendar.repository.TaskAssignmentPermissionRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class TaskAssignmentPermissionService {
    private final TaskAssignmentPermissionRepository taskAssignmentPermissionRepository;
    private final PermissionCheckService permissionCheckService;
    private final RoleLevelService roleLevelService;
    private final DepartmentService departmentService;
    public TaskAssignmentPermissionService(TaskAssignmentPermissionRepository taskAssignmentPermissionRepository,
                                           PermissionCheckService permissionCheckService,
                                           DepartmentService departmentService,
                                           RoleLevelService roleLevelService) {
        this.taskAssignmentPermissionRepository = taskAssignmentPermissionRepository;
        this.permissionCheckService = permissionCheckService;
        this.departmentService = departmentService;
        this.roleLevelService = roleLevelService;
    }

    public TaskAssignmentPermissionDTO createTaskAssignmentPermission(TaskAssignmentPermissionDTO taskAssignmentPermissionDTO) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "CREATE_ASSIGNMENT_RULE")) {
            throw new AccessDeniedException("Нет прав на создание правил назначения");
        }

        Department department = departmentService.getDepartmentById(taskAssignmentPermissionDTO.getDepartmentId());
        RoleLevel roleLevel = roleLevelService.getRoleById(taskAssignmentPermissionDTO.getRoleLevelId());

        TaskAssignmentPermission permission = new TaskAssignmentPermission();
        permission.setAllowed(taskAssignmentPermissionDTO.isAllowed());
        permission.setCreatedAt(LocalDateTime.now());
        permission.setRoleLevel(roleLevel);
        permission.setTargetDepartment(department);
        taskAssignmentPermissionRepository.save(permission);
        return convertToDTO(permission);
    }

    public TaskAssignmentPermissionDTO update(TaskAssignmentPermissionDTO taskAssignmentPermissionDTO, Long id) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "EDIT_ASSIGNMENT_RULE")) {
            throw new AccessDeniedException("Нет прав на редактирование правил назначения");
        }

        TaskAssignmentPermission permission = taskAssignmentPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Разрешение по такому ID " + id + " нет."));

        permission.setAllowed(taskAssignmentPermissionDTO.isAllowed());
        permission.setRoleLevel(roleLevelService.getRoleById(taskAssignmentPermissionDTO.getRoleLevelId()));
        permission.setTargetDepartment(departmentService.getDepartmentById(taskAssignmentPermissionDTO.getDepartmentId()));
        taskAssignmentPermissionRepository.save(permission);
        return convertToDTO(permission);
    }

    public TaskAssignmentPermissionDTO getTaskAssignmentPermission(Long id) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "VIEW_ASSIGNMENT_RULES")) {
            throw new AccessDeniedException("Нет прав на просмотр правил назначения");
        }

        TaskAssignmentPermission permission = taskAssignmentPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Разрешение по такому ID " + id + " нет."));

        return convertToDTO(permission);
    }

    public List<TaskAssignmentPermissionDTO> getTaskAssignmentPermissions() {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "VIEW_ASSIGNMENT_RULES")) {
            throw new AccessDeniedException("Нет прав на просмотр правил назначения");
        }

        return taskAssignmentPermissionRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<TaskAssignmentPermissionDTO> getTaskAssignmentPermissionsByDepartment(Long departmentId) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "VIEW_ASSIGNMENT_RULES")) {
            throw new AccessDeniedException("Нет прав на просмотр правил назначения");
        }

        Department department = departmentService.getDepartmentById(departmentId);
        return taskAssignmentPermissionRepository.findByTargetDepartment(department).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<TaskAssignmentPermissionDTO> getTaskAssignmentPermissionsByRoleLevel(Long roleLevelId) {
        User user = getCurrentUser();

        if (user.getRoleLevel() == null) {
            return Collections.emptyList();
        }

        if (!permissionCheckService.hasPermission(user, "VIEW_ASSIGNMENT_RULES")) {
            throw new AccessDeniedException("Нет прав на просмотр правил назначения");
        }

        RoleLevel roleLevel = roleLevelService.getRoleById(roleLevelId);
        return taskAssignmentPermissionRepository.findByRoleLevel(roleLevel).stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<DepartmentDTO> getMyAvailableDepartments() {
        User user = getCurrentUser();

        RoleLevel roleLevel = user.getRoleLevel();
        List<TaskAssignmentPermission> task = taskAssignmentPermissionRepository.findByRoleLevelAndAllowedTrue(roleLevel);
        List<DepartmentDTO> departments = new ArrayList<>();
        for (TaskAssignmentPermission taskAssignment : task) {
            DepartmentDTO dto = new DepartmentDTO();
            dto.setId(taskAssignment.getTargetDepartment().getId());
            dto.setName(taskAssignment.getTargetDepartment().getName());
            departments.add(dto);
        }
        return departments;
    }

    public void deleteTaskAssignmentPermission(Long id) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "DELETE_ASSIGNMENT_RULE")) {
            throw new AccessDeniedException("Нет прав на удаление правил назначения");
        }

        TaskAssignmentPermission permission = taskAssignmentPermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Разрешение по такому ID " + id + " нет."));

        taskAssignmentPermissionRepository.delete(permission);
    }

    private TaskAssignmentPermissionDTO convertToDTO(TaskAssignmentPermission taskAssignmentPermission) {
        TaskAssignmentPermissionDTO dto = new TaskAssignmentPermissionDTO();
        dto.setId(taskAssignmentPermission.getId());
        dto.setAllowed(taskAssignmentPermission.isAllowed());
        dto.setDepartmentId(taskAssignmentPermission.getTargetDepartment().getId());
        dto.setRoleLevelId(taskAssignmentPermission.getRoleLevel().getId());
        dto.setCreatedAt(taskAssignmentPermission.getCreatedAt());
        return dto;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }
}
