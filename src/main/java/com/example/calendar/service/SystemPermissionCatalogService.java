package com.example.calendar.service;

import com.example.calendar.DTO.SystemPermissionCatalogDTO;
import com.example.calendar.model.SystemPermissionCatalog;
import com.example.calendar.model.SystemPermissionKey;
import com.example.calendar.repository.SystemPermissionCatalogRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SystemPermissionCatalogService {
    private final SystemPermissionCatalogRepository catalogRepository;
    public SystemPermissionCatalogService(SystemPermissionCatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
    }

    public SystemPermissionCatalogDTO updateSystemPermissionCatalog(SystemPermissionCatalogDTO systemPermissionCatalogDTO) {
        SystemPermissionCatalog systemPermissionCatalog = catalogRepository.findByKey(systemPermissionCatalogDTO.getKey())
                .orElseThrow(() -> new RuntimeException("Правило для изменение не найдено"));

        //todo нужно сделать проверку прав
        systemPermissionCatalog.setName(systemPermissionCatalogDTO.getName());
        systemPermissionCatalog.setDescription(systemPermissionCatalogDTO.getDescription());
        systemPermissionCatalog.setGroup(systemPermissionCatalogDTO.getGroup());
        systemPermissionCatalog.setIcon(systemPermissionCatalogDTO.getIcon());
        catalogRepository.save(systemPermissionCatalog);
        return systemPermissionCatalogDTO;
    }

    public SystemPermissionCatalogDTO getSystemPermissionCatalogByName(String name) {
        SystemPermissionCatalog systemPermissionCatalog = catalogRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Задачи с названием " + name + " не была найдена"));

        //todo нужно сделать проверку прав
        return convertToDTO(systemPermissionCatalog);
    }

    public List<SystemPermissionCatalogDTO> getAllSystemPermissionCatalog() {
        return catalogRepository.getAllPermissionCatalog().stream()
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

    @PostConstruct
    public void initDefaultPermissions() {
        for (SystemPermissionKey key : SystemPermissionKey.values()) {
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
            case VIEW_ALL_TASKS: return "Все задачи";
            case VIEW_DEPARTMENT_TASKS: return "Задачи отдела";

            // ==================== УПРАВЛЕНИЕ ПРОЕКТАМИ ====================
            case CREATE_PROJECT: return "Создание проекта";
            case EDIT_PROJECT: return "Редактирование проекта";
            case DELETE_PROJECT: return "Удаление проекта";

            // ==================== УПРАВЛЕНИЕ РОЛЯМИ ====================
            case CREATE_ROLE: return "Создание роли";
            case EDIT_ROLE: return "Редактирование роли";
            case DELETE_ROLE: return "Удаление роли";
            case ASSIGN_ROLE: return "Назначение роли";

            // ==================== УПРАВЛЕНИЕ РАЗРЕШЕНИЯМИ ====================
            case CREATE_PERMISSION_SCHEME: return "Создание схемы";
            case EDIT_PERMISSION_SCHEME: return "Редактирование схемы";
            case DELETE_PERMISSION_SCHEME: return "Удаление схемы";
            case ADD_PERMISSION_TO_SCHEME: return "Добавление права в схему";

            // ==================== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ====================
            case INVITE_USER: return "Приглашение пользователя";
            case BLOCK_USER: return "Блокировка пользователя";
            case DELETE_USER: return "Удаление пользователя";

            // ==================== УПРАВЛЕНИЕ ДЕПАРТАМЕНТАМИ ====================
            case CREATE_DEPARTMENT: return "Создание отдела";
            case EDIT_DEPARTMENT: return "Редактирование отдела";
            case DELETE_DEPARTMENT: return "Удаление отдела";

            // ==================== ОТЧЕТЫ И НАСТРОЙКИ ====================
            case VIEW_REPORTS: return "Просмотр отчетов";
            case MANAGE_COMPANY_SETTINGS: return "Настройки компании";

            // На всякий случай, если забудешь добавить новое право
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

            // ==================== УПРАВЛЕНИЕ ПРОЕКТАМИ ====================
            case CREATE_PROJECT: return "Создание новых проектов";
            case EDIT_PROJECT: return "Изменение параметров проекта";
            case DELETE_PROJECT: return "Удаление проекта";

            // ==================== УПРАВЛЕНИЕ РОЛЯМИ ====================
            case CREATE_ROLE: return "Создание новых ролей (например, Разработчик, Тестировщик)";
            case EDIT_ROLE: return "Изменение названия и описания роли";
            case DELETE_ROLE: return "Удаление роли (если не используется)";
            case ASSIGN_ROLE: return "Назначение роли пользователю";

            // ==================== УПРАВЛЕНИЕ РАЗРЕШЕНИЯМИ ====================
            case CREATE_PERMISSION_SCHEME: return "Создание новой схемы разрешений";
            case EDIT_PERMISSION_SCHEME: return "Изменение параметров схемы";
            case DELETE_PERMISSION_SCHEME: return "Удаление схемы разрешений";
            case ADD_PERMISSION_TO_SCHEME: return "Добавление конкретного разрешения в схему";

            // ==================== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ====================
            case INVITE_USER: return "Отправка приглашения новому пользователю по email";
            case BLOCK_USER: return "Временная блокировка доступа пользователя";
            case DELETE_USER: return "Полное удаление пользователя из системы";

            // ==================== УПРАВЛЕНИЕ ДЕПАРТАМЕНТАМИ ====================
            case CREATE_DEPARTMENT: return "Создание нового отдела";
            case EDIT_DEPARTMENT: return "Изменение названия и параметров отдела";
            case DELETE_DEPARTMENT: return "Удаление отдела";

            // ==================== ОТЧЕТЫ И НАСТРОЙКИ ====================
            case VIEW_REPORTS: return "Доступ к аналитическим отчетам";
            case MANAGE_COMPANY_SETTINGS: return "Управление SMTP, профилем компании и другими настройками";

            default: return "";
        }
    }

    private String getDefaultGroup(SystemPermissionKey key) {
        if (key.name().startsWith("CREATE_") || key.name().startsWith("EDIT_") ||
                key.name().startsWith("DELETE_") || key.name().startsWith("VIEW_")) {

            if (key.name().contains("TASK")) return "Задачи";
            if (key.name().contains("PROJECT")) return "Проекты";
            if (key.name().contains("ROLE")) return "Роли";
            if (key.name().contains("USER")) return "Пользователи";
            if (key.name().contains("DEPARTMENT")) return "Отделы";
            if (key.name().contains("PERMISSION")) return "Разрешения";
            if (key.name().contains("REPORT")) return "Отчеты";
        }

        // Ручное распределение по группам
        switch (key) {
            case CREATE_TASK: case EDIT_TASK: case DELETE_TASK:
            case ASSIGN_TASK: case CHANGE_TASK_STATUS: case VIEW_ALL_TASKS:
            case VIEW_DEPARTMENT_TASKS:
                return "Задачи";

            case CREATE_PROJECT: case EDIT_PROJECT: case DELETE_PROJECT:
                return "Проекты";

            case CREATE_ROLE: case EDIT_ROLE: case DELETE_ROLE: case ASSIGN_ROLE:
                return "Роли";

            case CREATE_PERMISSION_SCHEME: case EDIT_PERMISSION_SCHEME:
            case DELETE_PERMISSION_SCHEME: case ADD_PERMISSION_TO_SCHEME:
                return "Разрешения";

            case INVITE_USER: case BLOCK_USER: case DELETE_USER:
                return "Пользователи";

            case CREATE_DEPARTMENT: case EDIT_DEPARTMENT: case DELETE_DEPARTMENT:
                return "Отделы";

            case VIEW_REPORTS:
                return "Отчеты";

            case MANAGE_COMPANY_SETTINGS:
                return "Настройки";

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

        switch (key) {
            case CHANGE_TASK_STATUS: return "fa-tasks";
            case INVITE_USER: return "fa-envelope";
            case BLOCK_USER: return "fa-ban";
            case VIEW_REPORTS: return "fa-chart-bar";
            case MANAGE_COMPANY_SETTINGS: return "fa-cog";
            case ADD_PERMISSION_TO_SCHEME: return "fa-plus-circle";
            default: return "fa-circle";
        }
    }
}
