package com.example.calendar.service;

import com.example.calendar.DTO.AssignmentDepartmentDTO;
import com.example.calendar.model.*;
import com.example.calendar.repository.AssignmentDepartmentRepository;
import com.example.calendar.repository.DepartmentLevelRepository;
import com.example.calendar.repository.DepartmentRepository;
import com.example.calendar.repository.RoleRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AssignmentRuleService {
    private final AssignmentDepartmentRepository assignmentDepartmentRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentLevelRepository departmentLevelRepository;
    public AssignmentRuleService(AssignmentDepartmentRepository assignmentDepartmentRepository, RoleRepository roleRepository, DepartmentRepository departmentRepository, DepartmentLevelRepository departmentLevelRepository) {
        this.assignmentDepartmentRepository = assignmentDepartmentRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.departmentLevelRepository = departmentLevelRepository;
    }

    public AssignmentDepartmentDTO createAssignmentRule(AssignmentDepartmentDTO assignmentDepartmentDTO) {
        User admin = getCurrentUser();
        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new AccessDeniedException("Only the admin can create the rules.");
        }

        Department sourceDepartment = departmentRepository.findByNameAndCompany(assignmentDepartmentDTO.getSourceDepartment(), admin.getCompany())
                .orElseThrow(() -> new IllegalArgumentException("No department exists."));

        Department targetDepartment = departmentRepository.findByNameAndCompany(assignmentDepartmentDTO.getTargetDepartment(), admin.getCompany())//но тут может подправить
                .orElseThrow(() -> new IllegalArgumentException("No department exists."));

        RoleLevel roleLevel = departmentLevelRepository.findByDepartmentAndLevel(targetDepartment, assignmentDepartmentDTO.getLevel())
                .orElseThrow(() -> new RuntimeException("Нельзя назначить"));

        AssignmentDepartment assignmentDepartment = new AssignmentDepartment();
        assignmentDepartment.setSourceDepartment(sourceDepartment);
        assignmentDepartment.setTargetDepartment(targetDepartment);
        assignmentDepartment.setAllowed(assignmentDepartmentDTO.isAllowed());
        return convertToDTO(assignmentDepartmentRepository.save(assignmentDepartment));
    }

    public void deleteAssignmentRule(Long delAssignmentRule_id) {
        User admin = getCurrentUser();
        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new AccessDeniedException("Only the admin can remove the rules.");
        }
        //возможно добавить проверку, что из той же компании
        assignmentDepartmentRepository.deleteById(delAssignmentRule_id);
    }

    public List<AssignmentDepartmentDTO> getRulesByRole(String roleCode) {
        User admin = getCurrentUser();
        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new AccessDeniedException("Only the admin can get the rules.");
        }

        Company company = admin.getCompany();
        Role role = roleRepository.findByCodeAndCompany(roleCode, company)
                .orElseThrow(() -> new IllegalArgumentException("role not found"));

        List<AssignmentDepartment> rules = assignmentDepartmentRepository.findByRole(role);

        return rules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AssignmentDepartmentDTO> getAllAssignmentRules() {
        User admin = getCurrentUser();

        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new AccessDeniedException("Only the admin can get the rules.");
        }

        Company company = admin.getCompany();
        List<AssignmentDepartment> rules = assignmentDepartmentRepository.findByRole_Company(company);

        return rules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean canUserAssignToUser(User fromUser, User toUser){
        if ("ADMIN".equals(fromUser.getRole().getCode())) {
            return true;
        }

        return assignmentDepartmentRepository.existsByRoleAndSourceDepartmentAndTargetDepartmentAndAllowedTrue(
                fromUser.getRole(), fromUser.getDepartment(), toUser.getDepartment()
        );
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    private AssignmentDepartmentDTO convertToDTO(AssignmentDepartment rule) {
        AssignmentDepartmentDTO dto = new AssignmentDepartmentDTO();
        dto.setId(rule.getId());
        dto.setAllowed(rule.isAllowed());
        dto.setSourceDepartment(rule.getSourceDepartment().getName());
        dto.setTargetDepartment(rule.getTargetDepartment().getName());
        return dto;
    }
}
