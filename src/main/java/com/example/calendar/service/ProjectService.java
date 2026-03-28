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
    private final PermissionCheckService permissionCheckService;

    public ProjectService(ProjectRepository projectRepository,
                          DepartmentRepository departmentRepository,
                          PermissionCheckService permissionCheckService) {
        this.projectRepository = projectRepository;
        this.departmentRepository = departmentRepository;
        this.permissionCheckService = permissionCheckService;
    }

    public ProjectDTO create(ProjectDTO projectDTO) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "CREATE_PROJECT")) {
            throw new SecurityException("Недостаточно прав для создания проекта");
        }

        Department department = null;
        if (projectDTO.getDepartmentId() != null) {
            department = departmentRepository.findById(projectDTO.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Отдел не найден"));

            if (!department.getCompany().getId().equals(user.getCompany().getId())) {
                throw new SecurityException("Отдел не принадлежит вашей компании");
            }
        } else {
            department = user.getDepartment();
            if (department == null) {
                throw new IllegalArgumentException("У пользователя не указан отдел");
            }
        }

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

        if (!permissionCheckService.hasPermission(user, "EDIT_PROJECT")) {
            throw new SecurityException("Недостаточно прав для обновления проекта");
        }

        Project project = projectRepository.findByIdAndDepartment_Company(
                        projectDTO.getId(), user.getCompany())
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден в данной компании"));

        if (!canUserModifyProject(user, project)) {
            throw new SecurityException("Нет прав для редактирования этого проекта");
        }

        project.setName(projectDTO.getName());

        if (projectDTO.getDepartmentId() != null &&
                !projectDTO.getDepartmentId().equals(project.getDepartment().getId())) {

            Department newDepartment = departmentRepository.findById(projectDTO.getDepartmentId())
                    .orElseThrow(() -> new IllegalArgumentException("Отдел не найден"));

            if (!newDepartment.getCompany().getId().equals(user.getCompany().getId())) {
                throw new SecurityException("Новый отдел не принадлежит вашей компании");
            }

            project.setDepartment(newDepartment);
        }

        return convertToDTO(projectRepository.save(project));
    }

    public List<ProjectDTO> findAll() {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "VIEW_PROJECTS")) {
            throw new SecurityException("Недостаточно прав для просмотра проектов");
        }

        List<Project> projects;

        if (user.getSystemRole() == SystemRole.COMPANY_OWNER) {
            projects = projectRepository.findByDepartment_Company(user.getCompany());
        } else if (user.getSystemRole() == SystemRole.DEPARTMENT_HEAD) {
            if (user.getDepartment() == null) {
                throw new IllegalArgumentException("У руководителя отдела не указан отдел");
            }
            projects = projectRepository.findByDepartment(user.getDepartment());
        } else {
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

        if (!permissionCheckService.hasPermission(user, "VIEW_PROJECT_DETAILS")) {
            throw new SecurityException("Недостаточно прав для просмотра деталей проекта");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден"));

        if (!hasAccessToProject(user, project)) {
            throw new SecurityException("Нет доступа к этому проекту");
        }

        return convertToDTO(project);
    }

    public void deleteById(Long id) {
        User user = getCurrentUser();

        if (!permissionCheckService.hasPermission(user, "DELETE_PROJECT")) {
            throw new SecurityException("Недостаточно прав для удаления проекта");
        }

        Project project = projectRepository.findByIdAndDepartment_Company(id, user.getCompany())
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден или нет прав для удаления"));

        if (user.getSystemRole() == SystemRole.DEPARTMENT_HEAD &&
                !project.getDepartment().getId().equals(user.getDepartment().getId())) {
            throw new SecurityException("Можно удалять только проекты своего отдела");
        }

        projectRepository.delete(project);
    }

    private boolean hasAccessToProject(User user, Project project) {
        if (!project.getDepartment().getCompany().getId().equals(user.getCompany().getId())) {
            return false;
        }

        if (user.getSystemRole() == SystemRole.COMPANY_OWNER) {
            return true;
        }

        if (user.getSystemRole() == SystemRole.DEPARTMENT_HEAD) {
            return project.getDepartment().getId().equals(user.getDepartment().getId());
        }

        return project.getDepartment().getId().equals(user.getDepartment().getId());
    }

    private boolean canUserModifyProject(User user, Project project) {
        if (user.getSystemRole() == SystemRole.COMPANY_OWNER) {
            return true;
        }

        if (user.getSystemRole() == SystemRole.DEPARTMENT_HEAD) {
            return project.getDepartment().getId().equals(user.getDepartment().getId());
        }

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

        if (project.getDepartment() != null) {
            projectDTO.setDepartmentId(project.getDepartment().getId());
            projectDTO.setDepartmentName(project.getDepartment().getName());

            if (project.getDepartment().getCompany() != null) {
                projectDTO.setCompanyId(project.getDepartment().getCompany().getId());
                projectDTO.setCompanyName(project.getDepartment().getCompany().getName());
            }
        }

        return projectDTO;
    }
}