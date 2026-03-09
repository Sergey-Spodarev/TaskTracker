package com.example.calendar.service;

import com.example.calendar.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PermissionCheckService {
    public boolean hasPermission(User user, String action) {
        if (user.getSystemRole() == SystemRole.COMPANY_OWNER) {
            return true;
        }

        if (user.getRoleLevel() == null) {
            return false;
        }

        if (user.getRoleLevel().getPermissionScheme() == null) {
            return false;
        }

        if (user.getRoleLevel().getPermissionScheme().getSystemPermissionDefinition() == null) {
            return false;
        }

        return checkCustomPermission(user, action) || checkSystemPermission(user, action);
    }

    private boolean checkSystemPermission(User user, String action) {
        SystemPermissionDefinition systemPermissionDefinition = user.getRoleLevel().getPermissionScheme().getSystemPermissionDefinition();
        boolean hasPermission = false;
        for (SystemPermissionCatalog systemPermissionCatalog : systemPermissionDefinition.getSystemPermissionCatalog()) {
            if (systemPermissionCatalog.getKey().name().equals(action)) {
                hasPermission = true;
                break;
            }
        }
        return hasPermission;
    }

    private boolean checkCustomPermission(User user, String action) {
        boolean hasPermission = false;
        CustomPermission customPermission = user.getRoleLevel().getPermissionScheme().getCustomPermission();
        if (customPermission == null)
            return false;

        int userLevel = user.getRoleLevel().getLevel();
        for (SchemePermission schemePermission : customPermission.getSchemePermission()) {
            if (schemePermission.getPermissionKey().equals(action)) {
                if (schemePermission.getMinLevel() <= userLevel && schemePermission.getMaxLevel() >= userLevel) {
                    hasPermission = true;
                    break;
                }
            }
        }
        return hasPermission;
    }
}
