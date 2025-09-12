package com.example.calendar.service;

import com.example.calendar.DTO.ProjectDTO;
import com.example.calendar.model.Company;
import com.example.calendar.model.Project;
import com.example.calendar.model.User;
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
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectDTO create(ProjectDTO projectDTO) {
        User user = getCurrentUser();
        Company company = user.getCompany();

        if (projectRepository.existsByProjectKeyAndCompany(projectDTO.getProjectKey(), company)) {
            throw new IllegalArgumentException("Проект с таким ключом уже существует");
        }

        Project project = new Project();
        project.setName(projectDTO.getName());
        project.setProjectKey(projectDTO.getProjectKey());
        project.setCompany(company);
        return convertToDTO(projectRepository.save(project));
    }

    public ProjectDTO update(ProjectDTO projectDTO) {
        User user = getCurrentUser();
        Company company = user.getCompany();

        Project project = projectRepository.findByIdAndCompany(projectDTO.getId(), company)
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден в данной компании"));

        project.setName(projectDTO.getName());
        return convertToDTO(projectRepository.save(project));
    }

    public List<ProjectDTO> findAll() {
        User user = getCurrentUser();
        System.out.println("🔍 Поиск проектов для компании: " + user.getCompany().getId());

        List<Project> projects = projectRepository.findByCompanyId(user.getCompany().getId());
        System.out.println("✅ Найдено проектов: " + projects.size());

        List<ProjectDTO> dtos = projects.stream()
                .map(p -> {
                    System.out.println("📄 Конвертируем проект: id=" + p.getId() + ", key=" + p.getProjectKey());
                    return convertToDTO(p);
                })
                .toList();

        return dtos;
    }

    public ProjectDTO findById(Long id) {
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndCompany(id, user.getCompany())
                .orElseThrow(() -> new IllegalArgumentException("Данной задачи не существует"));
        return convertToDTO(project);
    }

    public void deleteById(Long id) {
        User user = getCurrentUser();
        Project project = projectRepository.findByIdAndCompany(id, user.getCompany())
                .orElseThrow(() -> new IllegalArgumentException("Невозможно удалить задачу"));
        projectRepository.delete(project);
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
        return projectDTO;
    }
}
