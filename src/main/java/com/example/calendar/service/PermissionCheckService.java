package com.example.calendar.service;

import com.example.calendar.model.*;
import com.example.calendar.repository.CustomPermissionRepository;
import com.example.calendar.repository.PermissionSchemeRepository;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PermissionCheckService {

    private final PermissionSchemeRepository permissionSchemeRepository;
    private final CustomPermissionRepository customPermissionRepository;

    public PermissionCheckService(PermissionSchemeRepository permissionSchemeRepository,
                                  CustomPermissionRepository customPermissionRepository) {
        this.permissionSchemeRepository = permissionSchemeRepository;
        this.customPermissionRepository = customPermissionRepository;
    }

    public boolean hasPermission(User user, String action) {
        if (user.getSystemRole() == SystemRole.COMPANY_OWNER) {
            return true;
        }

        if (user.getRoleLevel() == null || user.getRoleLevel().getPermissionScheme() == null) {
            return false;
        }

        // ✅ Загружаем PermissionScheme со всеми необходимыми коллекциями
        PermissionScheme scheme = permissionSchemeRepository
                .findByIdWithPermissions(user.getRoleLevel().getPermissionScheme().getId())
                .orElse(user.getRoleLevel().getPermissionScheme());

        return checkCustomPermission(user, action, scheme) || checkSystemPermission(user, action, scheme);
    }

    private boolean checkSystemPermission(User user, String action, PermissionScheme scheme) {
        SystemPermissionDefinition systemPermissionDefinition = scheme.getSystemPermissionDefinition();
        if (systemPermissionDefinition == null) return false;

        for (SystemPermissionCatalog systemPermissionCatalog : systemPermissionDefinition.getSystemPermissionCatalog()) {
            if (systemPermissionCatalog.getKey().name().equals(action)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkCustomPermission(User user, String action, PermissionScheme scheme) {
        CustomPermission customPermission = scheme.getCustomPermission();
        if (customPermission == null) return false;

        int userLevel = user.getRoleLevel().getLevel();
        for (SchemePermission schemePermission : customPermission.getSchemePermission()) {
            if (schemePermission.getPermissionKey().equals(action)) {
                if (schemePermission.getMinLevel() <= userLevel && schemePermission.getMaxLevel() >= userLevel) {
                    return true;
                }
            }
        }
        return false;
    }
}