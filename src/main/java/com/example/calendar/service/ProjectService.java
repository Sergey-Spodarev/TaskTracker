package com.example.calendar.service;

import com.example.calendar.DTO.ProjectDTO;
import com.example.calendar.model.*;
import com.example.calendar.repository.DepartmentRepository;
import com.example.calendar.repository.ProjectRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final DepartmentRepository departmentRepository;
    private final SchemePermissionService schemePermissionService; // ← Добавляем проверку прав!

    public ProjectService(ProjectRepository projectRepository,
                          DepartmentRepository departmentRepository,
                          SchemePermissionService schemePermissionService) {
        this.projectRepository = projectRepository;
        this.departmentRepository = departmentRepository;
        this.schemePermissionService = schemePermissionService;
    }

    public ProjectDTO create(ProjectDTO projectDTO) {
        User user = getCurrentUser();

        // Проверка прав
        if (!schemePermissionService.hasPermission(user, "create_project")) {
            throw new SecurityException("Недостаточно прав для создания проекта");
        }

        // Получаем department из DTO или используем дефолтный
        Department department = null;
        if (projectDTO.getDepartmentId() != null) {
            department = departmentRepository.findById(projectDTO.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Отдел не найден"));

            // Проверяем, что отдел принадлежит компании пользователя
            if (!department.getCompany().getId().equals(user.getCompany().getId())) {
                throw new SecurityException("Отдел не принадлежит вашей компании");
            }
        } else {
            // Используем отдел пользователя как дефолтный
            department = user.getDepartment();
            if (department == null) {
                throw new IllegalArgumentException("У пользователя не указан отдел");
            }
        }

        // Проверяем уникальность ключа проекта в компании
        if (projectRepository.existsByProjectKeyAndDepartment_Company(
                projectDTO.getProjectKey(), user.getCompany())) {
            throw new IllegalArgumentException("Проект с таким ключом уже существует в компании");
        }

        Project project = new Project();
        project.setName(projectDTO.getName());
        project.setProjectKey(projectDTO.getProjectKey());
        project.setDepartment(department);

        return convertToDTO(projectRepository.save(project));
    }

    public ProjectDTO update(ProjectDTO projectDTO) {
        User user = getCurrentUser();

        // Проверка прав
        if (!schemePermissionService.hasPermission(user, "update_project")) {
            throw new SecurityException("Недостаточно прав для обновления проекта");
        }

        Project project = projectRepository.findByIdAndDepartment_Company(
                        projectDTO.getId(), user.getCompany())
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден в данной компании"));

        // Проверяем, может ли пользователь редактировать этот проект
        if (!canUserModifyProject(user, project)) {
            throw new SecurityException("Нет прав для редактирования этого проекта");
        }

        project.setName(projectDTO.getName());

        // Если нужно изменить отдел
        if (projectDTO.getDepartmentId() != null &&
                !projectDTO.getDepartmentId().equals(project.getDepartment().getId())) {

            Department newDepartment = departmentRepository.findById(projectDTO.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Отдел не найден"));

            // Проверяем, что новый отдел в той же компании
            if (!newDepartment.getCompany().getId().equals(user.getCompany().getId())) {
                throw new SecurityException("Новый отдел не принадлежит вашей компании");
            }

            project.setDepartment(newDepartment);
        }

        return convertToDTO(projectRepository.save(project));
    }

    public List<ProjectDTO> findAll() {
        User user = getCurrentUser();

        // Проверка прав
        if (!schemePermissionService.hasPermission(user, "view_projects")) {
            throw new SecurityException("Недостаточно прав для просмотра проектов");
        }

        List<Project> projects;

        // Разные уровни доступа
        if (user.getSystemRole() == SystemRole.COMPANY_OWNER) {
            // Владелец видит все проекты компании
            projects = projectRepository.findByDepartment_Company(user.getCompany());
        } else if (user.getSystemRole() == SystemRole.DEPARTMENT_HEAD) {
            // Руководитель отдела видит проекты своего отдела
            if (user.getDepartment() == null) {
                throw new IllegalArgumentException("У руководителя отдела не указан отдел");
            }
            projects = projectRepository.findByDepartment(user.getDepartment());
        } else {
            // Обычный пользователь видит проекты своего отдела
            if (user.getDepartment() == null) {
                throw new IllegalArgumentException("У пользователя не указан отдел");
            }
            projects = projectRepository.findByDepartment(user.getDepartment());
        }

        return projects.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public ProjectDTO findById(Long id) {
        User user = getCurrentUser();

        // Проверка прав
        if (!schemePermissionService.hasPermission(user, "view_project_details")) {
            throw new SecurityException("Недостаточно прав для просмотра деталей проекта");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден"));

        // Проверяем доступ к проекту
        if (!hasAccessToProject(user, project)) {
            throw new SecurityException("Нет доступа к этому проекту");
        }

        return convertToDTO(project);
    }

    public void deleteById(Long id) {
        User user = getCurrentUser();

        // Проверка прав
        if (!schemePermissionService.hasPermission(user, "delete_project")) {
            throw new SecurityException("Недостаточно прав для удаления проекта");
        }

        Project project = projectRepository.findByIdAndDepartment_Company(id, user.getCompany())
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден или нет прав для удаления"));

        // Дополнительная проверка для руководителей отделов
        if (user.getSystemRole() == SystemRole.DEPARTMENT_HEAD &&
                !project.getDepartment().getId().equals(user.getDepartment().getId())) {
            throw new SecurityException("Можно удалять только проекты своего отдела");
        }

        projectRepository.delete(project);
    }

    // Вспомогательные методы
    private boolean hasAccessToProject(User user, Project project) {
        // 1. Проверяем компанию
        if (!project.getDepartment().getCompany().getId().equals(user.getCompany().getId())) {
            return false;
        }

        // 2. Проверяем права доступа
        if (user.getSystemRole() == SystemRole.COMPANY_OWNER) {
            return true;
        }

        if (user.getSystemRole() == SystemRole.DEPARTMENT_HEAD) {
            return project.getDepartment().getId().equals(user.getDepartment().getId());
        }

        // Обычные пользователи видят проекты своего отдела
        return project.getDepartment().getId().equals(user.getDepartment().getId());
    }

    private boolean canUserModifyProject(User user, Project project) {
        if (user.getSystemRole() == SystemRole.COMPANY_OWNER) {
            return true;
        }

        if (user.getSystemRole() == SystemRole.DEPARTMENT_HEAD) {
            return project.getDepartment().getId().equals(user.getDepartment().getId());
        }

        // Обычные пользователи не могут редактировать проекты
        return false;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private ProjectDTO convertToDTO(Project project) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(project.getId());
        projectDTO.setName(project.getName());
        projectDTO.setProjectKey(project.getProjectKey());

        // Добавляем информацию об отделе
        if (project.getDepartment() != null) {
            projectDTO.setDepartmentId(project.getDepartment().getId());
            projectDTO.setDepartmentName(project.getDepartment().getName());

            // Добавляем информацию о компании (опционально)
            if (project.getDepartment().getCompany() != null) {
                projectDTO.setCompanyId(project.getDepartment().getCompany().getId());
                projectDTO.setCompanyName(project.getDepartment().getCompany().getName());
            }
        }

        return projectDTO;
    }
}