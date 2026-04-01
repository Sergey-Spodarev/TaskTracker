package com.example.calendar.service;

import com.example.calendar.DTO.SystemPermissionCatalogDTO;
import com.example.calendar.model.SystemPermissionCatalog;
import com.example.calendar.model.SystemPermissionKey;
import com.example.calendar.model.User;
import com.example.calendar.repository.SystemPermissionCatalogRepository;
import com.example.calendar.security.CustomUserDetails;
import jakarta.annotation.PostConstruct;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SystemPermissionCatalogService {
    private final SystemPermissionCatalogRepository catalogRepository;
    private final PermissionCheckService permissionCheckService;

    public SystemPermissionCatalogService(SystemPermissionCatalogRepository catalogRepository,
                                          PermissionCheckService permissionCheckService) {
        this.catalogRepository = catalogRepository;
        this.permissionCheckService = permissionCheckService;
    }

    public SystemPermissionCatalogDTO updateSystemPermissionCatalog(SystemPermissionCatalogDTO systemPermissionCatalogDTO) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "MANAGE_PERMISSION_CATALOG")) {
            throw new RuntimeException("У вас нет прав для обновление данных");
        }

        SystemPermissionCatalog systemPermissionCatalog = catalogRepository.findByKey(systemPermissionCatalogDTO.getKey())
                .orElseThrow(() -> new RuntimeException("Правило для изменение не найдено"));

        systemPermissionCatalog.setName(systemPermissionCatalogDTO.getName());
        systemPermissionCatalog.setDescription(systemPermissionCatalogDTO.getDescription());
        systemPermissionCatalog.setGroup(systemPermissionCatalogDTO.getGroup());
        systemPermissionCatalog.setIcon(systemPermissionCatalogDTO.getIcon());
        catalogRepository.save(systemPermissionCatalog);
        return systemPermissionCatalogDTO;
    }

    public SystemPermissionCatalogDTO getSystemPermissionCatalogByName(String name) {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "VIEW_PERMISSION_CATALOG")) {
            throw new RuntimeException("У вас нет прав для получение данных");
        }

        SystemPermissionCatalog systemPermissionCatalog = catalogRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Задачи с названием " + name + " не была найдена"));

        return convertToDTO(systemPermissionCatalog);
    }

    public Boolean existsSystemPermissionCatalogByKey(SystemPermissionKey key) {
        return catalogRepository.existsByKey(key);
    }

    public SystemPermissionCatalog getByKey(SystemPermissionKey key) {
        return catalogRepository.findByKey(key)
                .orElseThrow(() -> new RuntimeException("Задачи с ключом " + key + " не был найден"));
    }

    public List<SystemPermissionCatalogDTO> getAllSystemPermissionCatalog() {
        User user = getCurrentUser();
        if (!permissionCheckService.hasPermission(user, "VIEW_PERMISSION_CATALOG")) {
            throw new RuntimeException("У вас нет прав для получения данных");
        }

        return catalogRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    private SystemPermissionCatalogDTO convertToDTO(SystemPermissionCatalog systemPermissionCatalog) {
        SystemPermissionCatalogDTO dto = new SystemPermissionCatalogDTO();
        dto.setKey(systemPermissionCatalog.getKey());
        dto.setName(systemPermissionCatalog.getName());
        dto.setDescription(systemPermissionCatalog.getDescription());
        dto.setGroup(systemPermissionCatalog.getGroup());
        dto.setIcon(systemPermissionCatalog.getIcon());
        return dto;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return customUserDetails.getUser();
    }

    @PostConstruct
    public void initDefaultPermissions() {
        for (SystemPermissionKey key : SystemPermissionKey.values()) {
            try {
                if (!catalogRepository.existsByKey(key)) {
                    SystemPermissionCatalog permission = new SystemPermissionCatalog();
                    permission.setKey(key);
                    permission.setName(getDefaultDisplayName(key));
                    permission.setDescription(getDefaultDescription(key));
                    permission.setGroup(getDefaultGroup(key));
                    permission.setIcon(getDefaultIcon(key));
                    permission.setBuiltIn(true);

                    catalogRepository.save(permission);
                }
            } catch (Exception e) {
                System.err.println("Не удалось создать permission для ключа: " + key);
                e.printStackTrace();
            }
        }
    }

    private String getDefaultDisplayName(SystemPermissionKey key) {
        switch (key) {
            // ==================== УПРАВЛЕНИЕ ЗАДАЧАМИ ====================
            case CREATE_TASK: return "Создание задачи";
            case EDIT_TASK: return "Редактирование задачи";
            case DELETE_TASK: return "Удаление задачи";
            case ASSIGN_TASK: return "Назначение задачи";
            case CHANGE_TASK_STATUS: return "Изменение статуса";
            case VIEW_ALL_TASKS: return "Просмотр всех задач";
            case VIEW_DEPARTMENT_TASKS: return "Просмотр задач отдела";
            case VIEW_MY_TASKS: return "Мои задачи";
            case ADD_COMMENT: return "Добавление комментария";
            case DELETE_COMMENT: return "Удаление комментария";
            case EDIT_COMMENT: return "Редактирование комментария";
            case VIEW_CALENDAR: return "Просмотр календаря";

            // ==================== УПРАВЛЕНИЕ ПРОЕКТАМИ ====================
            case CREATE_PROJECT: return "Создание проекта";
            case EDIT_PROJECT: return "Редактирование проекта";
            case DELETE_PROJECT: return "Удаление проекта";
            case VIEW_ALL_PROJECTS: return "Просмотр всех проектов";
            case VIEW_PROJECT: return "Просмотр проекта";
            case VIEW_PROJECTS: return "Просмотр проектов";

            // ==================== УПРАВЛЕНИЕ РОЛЯМИ ====================
            case CREATE_ROLE: return "Создание роли";
            case EDIT_ROLE: return "Редактирование роли";
            case DELETE_ROLE: return "Удаление роли";
            case ASSIGN_ROLE: return "Назначение роли";
            case VIEW_ALL_ROLES: return "Просмотр всех ролей";
            case MANAGE_ROLES: return "Управление ролями";

            // ==================== УПРАВЛЕНИЕ УРОВНЯМИ РОЛЕЙ ====================
            case CREATE_ROLE_LEVEL: return "Создание уровня роли";
            case EDIT_ROLE_LEVEL: return "Редактирование уровня роли";
            case DELETE_ROLE_LEVEL: return "Удаление уровня роли";
            case VIEW_ALL_ROLE_LEVELS: return "Просмотр всех уровней ролей";
            case VIEW_ROLE_LEVEL: return "Просмотр уровня роли";
            case ASSIGN_ROLE_PERMISSIONS: return "Назначение прав роли";
            case VIEW_ROLE_PERMISSIONS: return "Просмотр прав роли";
            case VIEW_ROLE_LEVELS: return "Просмотр уровней ролей";

            // ==================== УПРАВЛЕНИЕ РАЗРЕШЕНИЯМИ ====================
            case CREATE_PERMISSION_SCHEME: return "Создание схемы разрешений";
            case EDIT_PERMISSION_SCHEME: return "Редактирование схемы разрешений";
            case DELETE_PERMISSION_SCHEME: return "Удаление схемы разрешений";
            case ADD_PERMISSION_TO_SCHEME: return "Добавление права в схему";
            case REMOVE_PERMISSION_FROM_SCHEME: return "Удаление права из схемы";
            case VIEW_ALL_PERMISSION_SCHEMES: return "Просмотр всех схем";
            case VIEW_PERMISSION_SCHEME: return "Просмотр схемы";
            case VIEW_PERMISSION_SCHEMES: return "Просмотр схем разрешений";

            // ==================== КАСТОМНЫЕ ПРАВА ====================
            case CREATE_CUSTOM_PERMISSION: return "Создание кастомного права";
            case EDIT_CUSTOM_PERMISSION: return "Редактирование кастомного права";
            case DELETE_CUSTOM_PERMISSION: return "Удаление кастомного права";
            case VIEW_ALL_CUSTOM_PERMISSIONS: return "Просмотр всех кастомных прав";
            case VIEW_CUSTOM_PERMISSION: return "Просмотр кастомного права";
            case ADD_SCHEME_LEVEL_TO_CUSTOM: return "Добавление уровня к праву";
            case REMOVE_SCHEME_LEVEL_FROM_CUSTOM: return "Удаление уровня из права";
            case VIEW_CUSTOM_PERMISSIONS: return "Просмотр кастомных прав";

            // ==================== УРОВНИ ДОСТУПА ====================
            case CREATE_SCHEME_PERMISSION: return "Создание уровня доступа";
            case EDIT_SCHEME_PERMISSION: return "Редактирование уровня доступа";
            case DELETE_SCHEME_PERMISSION: return "Удаление уровня доступа";
            case VIEW_ALL_SCHEME_PERMISSIONS: return "Просмотр всех уровней доступа";
            case VIEW_SCHEME_PERMISSION: return "Просмотр уровня доступа";
            case VIEW_SCHEME_PERMISSIONS: return "Просмотр уровней доступа";

            // ==================== НАБОРЫ ПРАВ ====================
            case CREATE_PERMISSION_DEFINITION: return "Создание набора прав";
            case EDIT_PERMISSION_DEFINITION: return "Редактирование набора прав";
            case DELETE_PERMISSION_DEFINITION: return "Удаление набора прав";
            case VIEW_ALL_PERMISSION_DEFINITIONS: return "Просмотр всех наборов прав";
            case VIEW_PERMISSION_DEFINITION: return "Просмотр набора прав";
            case VIEW_PERMISSION_DEFINITIONS: return "Просмотр наборов прав";

            // ==================== ПРАВИЛА НАЗНАЧЕНИЯ ЗАДАЧ ====================
            case VIEW_ASSIGNMENT_RULES: return "Просмотр правил назначения";
            case CREATE_ASSIGNMENT_RULE: return "Создание правила назначения";
            case EDIT_ASSIGNMENT_RULE: return "Редактирование правила назначения";
            case DELETE_ASSIGNMENT_RULE: return "Удаление правила назначения";

            // ==================== ПРАВА НА ПЕРЕДАЧУ ЗАДАЧ ====================
            case MANAGE_TASK_ASSIGNMENT_PERMISSIONS: return "Управление правами на передачу задач";

            // ==================== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ====================
            case INVITE_USER: return "Приглашение пользователя";
            case BLOCK_USER: return "Блокировка пользователя";
            case DELETE_USER: return "Удаление пользователя";
            case EDIT_USER: return "Редактирование пользователя";
            case VIEW_ALL_USERS: return "Просмотр всех пользователей";
            case VIEW_USER: return "Просмотр пользователя";
            case MANAGE_USERS: return "Управление пользователями";
            case VIEW_USERS: return "Просмотр пользователей";

            // ==================== УПРАВЛЕНИЕ ДЕПАРТАМЕНТАМИ ====================
            case CREATE_DEPARTMENT: return "Создание отдела";
            case EDIT_DEPARTMENT: return "Редактирование отдела";
            case DELETE_DEPARTMENT: return "Удаление отдела";
            case VIEW_ALL_DEPARTMENTS: return "Просмотр всех отделов";
            case VIEW_DEPARTMENT: return "Просмотр отдела";
            case ASSIGN_USER_DEPARTMENT: return "Назначение пользователя в отдел";
            case REMOVE_USER_FROM_DEPARTMENT: return "Удаление пользователя из отдела";
            case VIEW_DEPARTMENTS: return "Просмотр отделов";

            // ==================== УПРАВЛЕНИЕ КОМПАНИЕЙ ====================
            case EDIT_COMPANY: return "Редактирование компании";
            case VIEW_COMPANY: return "Просмотр компании";
            case MANAGE_COMPANY_SETTINGS: return "Управление настройками компании";

            // ==================== ОТЧЕТЫ ====================
            case VIEW_REPORTS: return "Просмотр отчетов";
            case EXPORT_DATA: return "Экспорт данных";
            case IMPORT_DATA: return "Импорт данных";

            // ==================== АДМИНИСТРИРОВАНИЕ ====================
            case VIEW_AUDIT_LOG: return "Просмотр логов аудита";
            case MANAGE_SYSTEM: return "Управление системой";
            case MANAGE_INTEGRATIONS: return "Управление интеграциями";
            case MANAGE_ASSIGNMENT_RULES: return "Управление правилами назначения";

            // ==================== УПРАВЛЕНИЕ КАТАЛОГОМ ====================
            case MANAGE_PERMISSION_CATALOG: return "Управление каталогом прав";
            case VIEW_PERMISSION_CATALOG: return "Просмотр каталога прав";

            case VIEW_TASKS: return "Просмотр задач";
            case ASSIGN_ROLE_DEPARTMENT: return "Назначение роли и отдела";
            case ACTIVATE_USER: return "Активация пользователя";

            default: return key.name();
        }
    }

    private String getDefaultDescription(SystemPermissionKey key) {
        switch (key) {
            // ==================== УПРАВЛЕНИЕ ЗАДАЧАМИ ====================
            case CREATE_TASK: return "Создание новых задач в проектах";
            case EDIT_TASK: return "Изменение названия, описания и сроков задачи";
            case DELETE_TASK: return "Полное удаление задачи из системы";
            case ASSIGN_TASK: return "Назначение исполнителя на задачу";
            case CHANGE_TASK_STATUS: return "Перевод задачи по статусам (В работе, Готово и т.д.)";
            case VIEW_ALL_TASKS: return "Просмотр всех задач компании без ограничений";
            case VIEW_DEPARTMENT_TASKS: return "Просмотр задач только своего отдела";
            case VIEW_MY_TASKS: return "Просмотр только своих задач";
            case ADD_COMMENT: return "Добавление комментариев к задачам";
            case DELETE_COMMENT: return "Удаление комментариев";
            case EDIT_COMMENT: return "Редактирование своих комментариев";
            case VIEW_CALENDAR: return "Просмотр календаря с задачами";

            // ==================== УПРАВЛЕНИЕ ПРОЕКТАМИ ====================
            case CREATE_PROJECT: return "Создание новых проектов";
            case EDIT_PROJECT: return "Изменение параметров проекта";
            case DELETE_PROJECT: return "Удаление проекта";
            case VIEW_ALL_PROJECTS: return "Просмотр всех проектов компании";
            case VIEW_PROJECT: return "Просмотр деталей конкретного проекта";
            case VIEW_PROJECTS: return "Просмотр списка проектов компании";

            // ==================== УПРАВЛЕНИЕ РОЛЯМИ ====================
            case CREATE_ROLE: return "Создание новых ролей (например, Разработчик, Тестировщик)";
            case EDIT_ROLE: return "Изменение названия и описания роли";
            case DELETE_ROLE: return "Удаление роли (если не используется)";
            case ASSIGN_ROLE: return "Назначение роли пользователю";
            case VIEW_ALL_ROLES: return "Просмотр всех ролей в системе";
            case MANAGE_ROLES: return "Полное управление ролями (создание, редактирование, удаление, назначение прав)";

            // ==================== УПРАВЛЕНИЕ УРОВНЯМИ РОЛЕЙ ====================
            case CREATE_ROLE_LEVEL: return "Создание нового уровня для роли";
            case EDIT_ROLE_LEVEL: return "Изменение параметров уровня роли";
            case DELETE_ROLE_LEVEL: return "Удаление уровня роли";
            case VIEW_ALL_ROLE_LEVELS: return "Просмотр всех уровней ролей";
            case VIEW_ROLE_LEVEL: return "Просмотр деталей уровня роли";
            case ASSIGN_ROLE_PERMISSIONS: return "Назначение прав конкретному уровню роли";
            case VIEW_ROLE_PERMISSIONS: return "Просмотр прав, назначенных уровню роли";
            case VIEW_ROLE_LEVELS: return "Просмотр уровней ролей";

            // ==================== УПРАВЛЕНИЕ РАЗРЕШЕНИЯМИ ====================
            case CREATE_PERMISSION_SCHEME: return "Создание новой схемы разрешений";
            case EDIT_PERMISSION_SCHEME: return "Изменение параметров схемы";
            case DELETE_PERMISSION_SCHEME: return "Удаление схемы разрешений";
            case ADD_PERMISSION_TO_SCHEME: return "Добавление конкретного разрешения в схему";
            case REMOVE_PERMISSION_FROM_SCHEME: return "Удаление разрешения из схемы";
            case VIEW_ALL_PERMISSION_SCHEMES: return "Просмотр всех схем разрешений";
            case VIEW_PERMISSION_SCHEME: return "Просмотр деталей схемы разрешений";
            case VIEW_PERMISSION_SCHEMES: return "Просмотр списка схем разрешений";

            // ==================== ПРАВИЛА НАЗНАЧЕНИЯ ЗАДАЧ ====================
            case VIEW_ASSIGNMENT_RULES: return "Просмотр списка правил: какие грейды в какие департаменты могут назначать задачи";
            case CREATE_ASSIGNMENT_RULE: return "Создание нового правила, разрешающего грейду доступ к департаменту";
            case EDIT_ASSIGNMENT_RULE: return "Изменение параметров существующего правила назначения";
            case DELETE_ASSIGNMENT_RULE: return "Удаление правила назначения задач";

            // ==================== ПРАВА НА ПЕРЕДАЧУ ЗАДАЧ ====================
            case MANAGE_TASK_ASSIGNMENT_PERMISSIONS: return "Создание и удаление правил передачи задач между отделами";

            // ==================== КАСТОМНЫЕ ПРАВА ====================
            case CREATE_CUSTOM_PERMISSION: return "Создание кастомного права";
            case EDIT_CUSTOM_PERMISSION: return "Редактирование кастомного права";
            case DELETE_CUSTOM_PERMISSION: return "Удаление кастомного права";
            case VIEW_ALL_CUSTOM_PERMISSIONS: return "Просмотр всех кастомных прав";
            case VIEW_CUSTOM_PERMISSION: return "Просмотр деталей кастомного права";
            case ADD_SCHEME_LEVEL_TO_CUSTOM: return "Добавление уровня доступа к кастомному праву";
            case REMOVE_SCHEME_LEVEL_FROM_CUSTOM: return "Удаление уровня доступа из кастомного права";
            case VIEW_CUSTOM_PERMISSIONS: return "Просмотр списка кастомных прав";

            // ==================== УРОВНИ ДОСТУПА ====================
            case CREATE_SCHEME_PERMISSION: return "Создание уровня доступа";
            case EDIT_SCHEME_PERMISSION: return "Редактирование уровня доступа";
            case DELETE_SCHEME_PERMISSION: return "Удаление уровня доступа";
            case VIEW_ALL_SCHEME_PERMISSIONS: return "Просмотр всех уровней доступа";
            case VIEW_SCHEME_PERMISSION: return "Просмотр деталей уровня доступа";
            case VIEW_SCHEME_PERMISSIONS: return "Просмотр списка уровней доступа";

            // ==================== НАБОРЫ ПРАВ ====================
            case CREATE_PERMISSION_DEFINITION: return "Создание набора прав";
            case EDIT_PERMISSION_DEFINITION: return "Редактирование набора прав";
            case DELETE_PERMISSION_DEFINITION: return "Удаление набора прав";
            case VIEW_ALL_PERMISSION_DEFINITIONS: return "Просмотр всех наборов прав";
            case VIEW_PERMISSION_DEFINITION: return "Просмотр деталей набора прав";
            case VIEW_PERMISSION_DEFINITIONS: return "Просмотр списка наборов прав";

            // ==================== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ====================
            case INVITE_USER: return "Отправка приглашения новому пользователю по email";
            case BLOCK_USER: return "Временная блокировка доступа пользователя";
            case DELETE_USER: return "Полное удаление пользователя из системы";
            case EDIT_USER: return "Редактирование данных пользователя";
            case VIEW_ALL_USERS: return "Просмотр всех пользователей компании";
            case VIEW_USER: return "Просмотр деталей пользователя";
            case MANAGE_USERS: return "Полное управление пользователями (все операции)";
            case VIEW_USERS: return "Просмотр списка пользователей компании";

            // ==================== УПРАВЛЕНИЕ ДЕПАРТАМЕНТАМИ ====================
            case CREATE_DEPARTMENT: return "Создание нового отдела";
            case EDIT_DEPARTMENT: return "Изменение названия и параметров отдела";
            case DELETE_DEPARTMENT: return "Удаление отдела";
            case VIEW_ALL_DEPARTMENTS: return "Просмотр всех отделов";
            case VIEW_DEPARTMENT: return "Просмотр деталей отдела";
            case ASSIGN_USER_DEPARTMENT: return "Назначение пользователя в отдел";
            case REMOVE_USER_FROM_DEPARTMENT: return "Удаление пользователя из отдела";
            case VIEW_DEPARTMENTS: return "Просмотр списка отделов компании";

            // ==================== УПРАВЛЕНИЕ КОМПАНИЕЙ ====================
            case EDIT_COMPANY: return "Редактирование информации о компании";
            case VIEW_COMPANY: return "Просмотр информации о компании";
            case MANAGE_COMPANY_SETTINGS: return "Управление SMTP, профилем компании и другими настройками";

            // ==================== ОТЧЕТЫ ====================
            case VIEW_REPORTS: return "Доступ к аналитическим отчетам";
            case EXPORT_DATA: return "Экспорт данных в различные форматы";
            case IMPORT_DATA: return "Импорт данных в систему";

            // ==================== АДМИНИСТРИРОВАНИЕ ====================
            case VIEW_AUDIT_LOG: return "Просмотр логов аудита системы";
            case MANAGE_SYSTEM: return "Управление системными настройками";
            case MANAGE_INTEGRATIONS: return "Управление интеграциями с внешними сервисами";
            case MANAGE_ASSIGNMENT_RULES: return "Управление правилами назначения задач между отделами";

            // ==================== УПРАВЛЕНИЕ КАТАЛОГОМ ====================
            case MANAGE_PERMISSION_CATALOG: return "Редактирование названий, описаний, иконок и групп системных прав";
            case VIEW_PERMISSION_CATALOG: return "Просмотр всех доступных системных прав в каталоге";

            case VIEW_TASKS: return "Просмотр списка задач";
            case ASSIGN_ROLE_DEPARTMENT: return "Назначение пользователю роли и отдела одновременно";
            case ACTIVATE_USER: return "Активация приглашенного пользователя в системе";

            default: return "";
        }
    }

    private String getDefaultGroup(SystemPermissionKey key) {
        // Автоматическое определение группы по ключу
        if (key.name().startsWith("CREATE_") || key.name().startsWith("EDIT_") ||
                key.name().startsWith("DELETE_") || key.name().startsWith("VIEW_") ||
                key.name().startsWith("ADD_") || key.name().startsWith("REMOVE_") ||
                key.name().startsWith("ASSIGN_") || key.name().startsWith("MANAGE_")) {

            if (key.name().contains("TASK")) return "Задачи";
            if (key.name().contains("PROJECT")) return "Проекты";
            if (key.name().contains("ROLE")) return "Роли";
            if (key.name().contains("USER")) return "Пользователи";
            if (key.name().contains("DEPARTMENT")) return "Отделы";
            if (key.name().contains("PERMISSION") && key.name().contains("SCHEME")) return "Схемы разрешений";
            if (key.name().contains("PERMISSION")) return "Разрешения";
            if (key.name().contains("SCHEME") && key.name().contains("PERMISSION")) return "Уровни доступа";
            if (key.name().contains("CUSTOM")) return "Кастомные права";
            if (key.name().contains("COMPANY")) return "Компания";
            if (key.name().contains("REPORT")) return "Отчеты";
            if (key.name().contains("EXPORT")) return "Отчеты";
            if (key.name().contains("IMPORT")) return "Отчеты";
            if (key.name().contains("AUDIT")) return "Администрирование";
            if (key.name().contains("SYSTEM")) return "Администрирование";
            if (key.name().contains("INTEGRATION")) return "Администрирование";
            if (key.name().contains("ASSIGNMENT")) return "Администрирование";
        }

        // Ручное распределение по группам
        switch (key) {
            case CREATE_TASK: case EDIT_TASK: case DELETE_TASK:
            case ASSIGN_TASK: case CHANGE_TASK_STATUS: case VIEW_ALL_TASKS:
            case VIEW_DEPARTMENT_TASKS: case VIEW_MY_TASKS: case ADD_COMMENT:
            case DELETE_COMMENT: case EDIT_COMMENT:
                return "Задачи";

            case CREATE_PROJECT: case EDIT_PROJECT: case DELETE_PROJECT:
            case VIEW_ALL_PROJECTS: case VIEW_PROJECT: case VIEW_PROJECTS:
                return "Проекты";

            case CREATE_ROLE: case EDIT_ROLE: case DELETE_ROLE: case ASSIGN_ROLE:
            case VIEW_ALL_ROLES: case MANAGE_ROLES:
                return "Роли";

            case CREATE_ROLE_LEVEL: case EDIT_ROLE_LEVEL: case DELETE_ROLE_LEVEL:
            case VIEW_ALL_ROLE_LEVELS: case VIEW_ROLE_LEVEL: case VIEW_ROLE_LEVELS:
            case ASSIGN_ROLE_PERMISSIONS: case VIEW_ROLE_PERMISSIONS:
                return "Уровни ролей";

            case CREATE_PERMISSION_SCHEME: case EDIT_PERMISSION_SCHEME:
            case DELETE_PERMISSION_SCHEME: case ADD_PERMISSION_TO_SCHEME:
            case REMOVE_PERMISSION_FROM_SCHEME: case VIEW_ALL_PERMISSION_SCHEMES:
            case VIEW_PERMISSION_SCHEME: case VIEW_PERMISSION_SCHEMES:
                return "Схемы разрешений";

            case VIEW_ASSIGNMENT_RULES:
            case CREATE_ASSIGNMENT_RULE:
            case EDIT_ASSIGNMENT_RULE:
            case DELETE_ASSIGNMENT_RULE:
                return "Правила назначения";

            case MANAGE_TASK_ASSIGNMENT_PERMISSIONS:
                return "Передача задач";

            case CREATE_CUSTOM_PERMISSION: case EDIT_CUSTOM_PERMISSION:
            case DELETE_CUSTOM_PERMISSION: case VIEW_ALL_CUSTOM_PERMISSIONS:
            case VIEW_CUSTOM_PERMISSION: case VIEW_CUSTOM_PERMISSIONS:
            case ADD_SCHEME_LEVEL_TO_CUSTOM: case REMOVE_SCHEME_LEVEL_FROM_CUSTOM:
                return "Кастомные права";

            case CREATE_SCHEME_PERMISSION: case EDIT_SCHEME_PERMISSION:
            case DELETE_SCHEME_PERMISSION: case VIEW_ALL_SCHEME_PERMISSIONS:
            case VIEW_SCHEME_PERMISSION: case VIEW_SCHEME_PERMISSIONS:
                return "Уровни доступа";

            case CREATE_PERMISSION_DEFINITION: case EDIT_PERMISSION_DEFINITION:
            case DELETE_PERMISSION_DEFINITION: case VIEW_ALL_PERMISSION_DEFINITIONS:
            case VIEW_PERMISSION_DEFINITION: case VIEW_PERMISSION_DEFINITIONS:
                return "Наборы прав";

            case INVITE_USER: case BLOCK_USER: case DELETE_USER: case EDIT_USER:
            case VIEW_ALL_USERS: case VIEW_USER: case MANAGE_USERS: case VIEW_USERS:
                return "Пользователи";

            case CREATE_DEPARTMENT: case EDIT_DEPARTMENT: case DELETE_DEPARTMENT:
            case VIEW_ALL_DEPARTMENTS: case VIEW_DEPARTMENT: case VIEW_DEPARTMENTS:
            case ASSIGN_USER_DEPARTMENT: case REMOVE_USER_FROM_DEPARTMENT:
                return "Отделы";

            case EDIT_COMPANY: case VIEW_COMPANY: case MANAGE_COMPANY_SETTINGS:
                return "Компания";

            case VIEW_REPORTS: case EXPORT_DATA: case IMPORT_DATA:
                return "Отчеты";

            case VIEW_AUDIT_LOG: case MANAGE_SYSTEM: case MANAGE_INTEGRATIONS:
            case MANAGE_ASSIGNMENT_RULES:
                return "Администрирование";

            case MANAGE_PERMISSION_CATALOG: case VIEW_PERMISSION_CATALOG:
                return "Каталог прав";

            case VIEW_TASKS: return "Задачи";
            case ASSIGN_ROLE_DEPARTMENT: return "Пользователи";
            case ACTIVATE_USER: return "Пользователи";

            default:
                return "Другое";
        }
    }

    private String getDefaultIcon(SystemPermissionKey key) {
        if (key.name().startsWith("CREATE")) return "fa-plus";
        if (key.name().startsWith("EDIT")) return "fa-edit";
        if (key.name().startsWith("DELETE")) return "fa-trash";
        if (key.name().startsWith("VIEW")) return "fa-eye";
        if (key.name().startsWith("ASSIGN")) return "fa-user-check";
        if (key.name().startsWith("ADD")) return "fa-plus-circle";
        if (key.name().startsWith("REMOVE")) return "fa-minus-circle";
        if (key.name().startsWith("MANAGE")) return "fa-cog";

        switch (key) {
            case CHANGE_TASK_STATUS: return "fa-tasks";
            case ADD_COMMENT: return "fa-comment";
            case EDIT_COMMENT: return "fa-comment-dots";
            case DELETE_COMMENT: return "fa-comment-slash";

            case INVITE_USER: return "fa-envelope";
            case BLOCK_USER: return "fa-ban";
            case MANAGE_USERS: return "fa-users-cog";
            case MANAGE_ROLES: return "fa-users-cog";

            case VIEW_REPORTS: return "fa-chart-bar";
            case EXPORT_DATA: return "fa-download";
            case IMPORT_DATA: return "fa-upload";

            case MANAGE_COMPANY_SETTINGS: return "fa-cog";
            case EDIT_COMPANY: return "fa-building";
            case VIEW_COMPANY: return "fa-building";

            case ADD_PERMISSION_TO_SCHEME: return "fa-plus-circle";
            case REMOVE_PERMISSION_FROM_SCHEME: return "fa-minus-circle";

            case VIEW_AUDIT_LOG: return "fa-history";
            case MANAGE_SYSTEM: return "fa-server";
            case MANAGE_INTEGRATIONS: return "fa-plug";
            case MANAGE_ASSIGNMENT_RULES: return "fa-rule";

            case VIEW_ASSIGNMENT_RULES: return "fa-list-alt";
            case CREATE_ASSIGNMENT_RULE: return "fa-plus-square";
            case EDIT_ASSIGNMENT_RULE: return "fa-edit";
            case DELETE_ASSIGNMENT_RULE: return "fa-trash-alt";

            case MANAGE_TASK_ASSIGNMENT_PERMISSIONS: return "fa-exchange-alt";

            case MANAGE_PERMISSION_CATALOG: return "fa-database";
            case VIEW_PERMISSION_CATALOG: return "fa-list";

            case CREATE_ROLE_LEVEL: return "fa-layer-group";
            case EDIT_ROLE_LEVEL: return "fa-layer-group";
            case DELETE_ROLE_LEVEL: return "fa-layer-group";
            case VIEW_ALL_ROLE_LEVELS: return "fa-layer-group";
            case VIEW_ROLE_LEVEL: return "fa-layer-group";
            case ASSIGN_ROLE_PERMISSIONS: return "fa-key";
            case VIEW_ROLE_PERMISSIONS: return "fa-key";

            case VIEW_TASKS: return "fa-tasks";
            case ASSIGN_USER_DEPARTMENT: return "fa-user-plus";
            case ASSIGN_ROLE_DEPARTMENT: return "fa-user-tag";
            case ACTIVATE_USER: return "fa-user-check";
            case REMOVE_USER_FROM_DEPARTMENT: return "fa-user-minus";
            case VIEW_DEPARTMENTS: return "fa-building";
            case VIEW_USERS: return "fa-users";
            case VIEW_PROJECTS: return "fa-folder-open";
            case VIEW_PERMISSION_SCHEMES: return "fa-shield-alt";
            case VIEW_CUSTOM_PERMISSIONS: return "fa-star";
            case VIEW_SCHEME_PERMISSIONS: return "fa-layer-group";
            case VIEW_PERMISSION_DEFINITIONS: return "fa-cubes";
            case VIEW_ROLE_LEVELS: return "fa-layer-group";

            default: return "fa-circle";
        }
    }
}