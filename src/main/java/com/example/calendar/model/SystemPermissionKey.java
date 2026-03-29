package com.example.calendar.model;

public enum SystemPermissionKey {

    // ==================== УПРАВЛЕНИЕ ЗАДАЧАМИ ====================
    CREATE_TASK,
    EDIT_TASK,
    DELETE_TASK,
    ASSIGN_TASK,
    CHANGE_TASK_STATUS,
    VIEW_ALL_TASKS,
    VIEW_DEPARTMENT_TASKS,
    VIEW_MY_TASKS,
    VIEW_TASKS,
    ADD_COMMENT,
    DELETE_COMMENT,
    EDIT_COMMENT,
    VIEW_CALENDAR,

    // ==================== УПРАВЛЕНИЕ ПРОЕКТАМИ ====================
    CREATE_PROJECT,
    EDIT_PROJECT,
    DELETE_PROJECT,
    VIEW_ALL_PROJECTS,
    VIEW_PROJECT,
    VIEW_PROJECTS,

    // ==================== УПРАВЛЕНИЕ РОЛЯМИ ====================
    CREATE_ROLE,
    EDIT_ROLE,
    DELETE_ROLE,
    ASSIGN_ROLE,
    VIEW_ALL_ROLES,
    MANAGE_ROLES,

    // ==================== УПРАВЛЕНИЕ УРОВНЯМИ РОЛЕЙ ====================
    CREATE_ROLE_LEVEL,
    EDIT_ROLE_LEVEL,
    DELETE_ROLE_LEVEL,
    VIEW_ALL_ROLE_LEVELS,
    VIEW_ROLE_LEVEL,
    ASSIGN_ROLE_PERMISSIONS,
    VIEW_ROLE_PERMISSIONS,
    VIEW_ROLE_LEVELS,

    // ==================== УПРАВЛЕНИЕ РАЗРЕШЕНИЯМИ ====================
    CREATE_PERMISSION_SCHEME,
    EDIT_PERMISSION_SCHEME,
    DELETE_PERMISSION_SCHEME,
    ADD_PERMISSION_TO_SCHEME,
    REMOVE_PERMISSION_FROM_SCHEME,
    VIEW_ALL_PERMISSION_SCHEMES,
    VIEW_PERMISSION_SCHEME,
    VIEW_PERMISSION_SCHEMES,

    // ==================== КАСТОМНЫЕ ПРАВА ====================
    CREATE_CUSTOM_PERMISSION,
    EDIT_CUSTOM_PERMISSION,
    DELETE_CUSTOM_PERMISSION,
    VIEW_ALL_CUSTOM_PERMISSIONS,
    VIEW_CUSTOM_PERMISSION,
    ADD_SCHEME_LEVEL_TO_CUSTOM,
    REMOVE_SCHEME_LEVEL_FROM_CUSTOM,
    VIEW_CUSTOM_PERMISSIONS,

    // ==================== УПРАВЛЕНИЕ УРОВНЯМИ (SCHEME_PERMISSIONS) ====================
    CREATE_SCHEME_PERMISSION,
    EDIT_SCHEME_PERMISSION,
    DELETE_SCHEME_PERMISSION,
    VIEW_ALL_SCHEME_PERMISSIONS,
    VIEW_SCHEME_PERMISSION,
    VIEW_SCHEME_PERMISSIONS,

    // ==================== УПРАВЛЕНИЕ НАБОРАМИ ====================
    CREATE_PERMISSION_DEFINITION,
    EDIT_PERMISSION_DEFINITION,
    DELETE_PERMISSION_DEFINITION,
    VIEW_ALL_PERMISSION_DEFINITIONS,
    VIEW_PERMISSION_DEFINITION,
    VIEW_PERMISSION_DEFINITIONS,

    // ==================== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ====================
    INVITE_USER,
    BLOCK_USER,
    DELETE_USER,
    EDIT_USER,
    VIEW_ALL_USERS,
    VIEW_USER,
    MANAGE_USERS,
    VIEW_USERS,
    ASSIGN_USER_DEPARTMENT,
    ASSIGN_ROLE_DEPARTMENT,
    ACTIVATE_USER,

    // ==================== УПРАВЛЕНИЕ ДЕПАРТАМЕНТАМИ ====================
    CREATE_DEPARTMENT,
    EDIT_DEPARTMENT,
    DELETE_DEPARTMENT,
    VIEW_ALL_DEPARTMENTS,
    VIEW_DEPARTMENT,
    REMOVE_USER_FROM_DEPARTMENT,
    VIEW_DEPARTMENTS,

    // ==================== УПРАВЛЕНИЕ КОМПАНИЕЙ ====================
    EDIT_COMPANY,
    VIEW_COMPANY,
    MANAGE_COMPANY_SETTINGS,

    // ==================== ОТЧЕТЫ ====================
    VIEW_REPORTS,
    EXPORT_DATA,
    IMPORT_DATA,

    // ==================== АДМИНИСТРИРОВАНИЕ ====================
    VIEW_AUDIT_LOG,
    MANAGE_SYSTEM,
    MANAGE_INTEGRATIONS,
    MANAGE_ASSIGNMENT_RULES,

    // ==================== УПРАВЛЕНИЕ КАТАЛОГОМ ====================
    MANAGE_PERMISSION_CATALOG,
    VIEW_PERMISSION_CATALOG
}