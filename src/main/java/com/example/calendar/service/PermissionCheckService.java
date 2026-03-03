package com.example.calendar.service;

import com.example.calendar.model.SystemPermissionCatalog;
import com.example.calendar.model.SystemPermissionDefinition;
import com.example.calendar.model.SystemRole;
import com.example.calendar.model.User;
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
}
