package com.example.calendar.service;

import com.example.calendar.DTO.AssignmentRuleDTO;
import com.example.calendar.model.*;
import com.example.calendar.repository.AssignmentRuleRepository;
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
    private final AssignmentRuleRepository assignmentRuleRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    public AssignmentRuleService(AssignmentRuleRepository assignmentRuleRepository, RoleRepository roleRepository, DepartmentRepository departmentRepository) {
        this.assignmentRuleRepository = assignmentRuleRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
    }

    public AssignmentRuleDTO createAssignmentRule(AssignmentRuleDTO assignmentRuleDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();
        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new AccessDeniedException("Only the admin can create the rules.");
        }

        Role role = roleRepository.findByCode(assignmentRuleDTO.getRoleCode())
                .orElseThrow(() -> new IllegalArgumentException("No role exists."));

        Department sourceDepartment = departmentRepository.findByName(assignmentRuleDTO.getSourceDepartment())
                .orElseThrow(() -> new IllegalArgumentException("No department exists."));

        Department targetDepartment = departmentRepository.findByName(assignmentRuleDTO.getTargetDepartment())
                .orElseThrow(() -> new IllegalArgumentException("No department exists."));

        AssignmentRule assignmentRule = new AssignmentRule();
        assignmentRule.setRole(role);
        assignmentRule.setSourceDepartment(sourceDepartment);
        assignmentRule.setTargetDepartment(targetDepartment);
        assignmentRule.setAllowed(assignmentRuleDTO.isAllowed());
        return convertToDTO(assignmentRuleRepository.save(assignmentRule));
    }

    public void deleteAssignmentRule(Long delAssignmentRule_id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();
        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new AccessDeniedException("Only the admin can remove the rules.");
        }
        //возможно добавить проверку, что из той же компании
        assignmentRuleRepository.deleteById(delAssignmentRule_id);
    }

    public List<AssignmentRuleDTO> getRulesByRole(String roleCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();
        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new AccessDeniedException("Only the admin can get the rules.");
        }

        Company company = admin.getCompany();
        Role role = roleRepository.findByCodeAndCompany(roleCode, company)
                .orElseThrow(() -> new IllegalArgumentException("role not found"));

        List<AssignmentRule> rules = assignmentRuleRepository.findByRole(role)
                .orElseThrow(() -> new IllegalArgumentException("No rule exists."));

        return rules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<AssignmentRuleDTO> getAllAssignmentRules() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();

        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new AccessDeniedException("Only the admin can get the rules.");
        }

        Company company = admin.getCompany();
        List<AssignmentRule> rules = assignmentRuleRepository.findByRole_Company(company)
                .orElseThrow(() -> new IllegalArgumentException("No rule exists."));

        return rules.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private AssignmentRuleDTO convertToDTO(AssignmentRule rule) {
        AssignmentRuleDTO dto = new AssignmentRuleDTO();
        dto.setId(rule.getId());
        dto.setAllowed(rule.isAllowed());
        dto.setRoleCode(rule.getRole().getCode());
        dto.setRoleName(rule.getRole().getDisplayName());
        dto.setSourceDepartment(rule.getSourceDepartment().getName());
        dto.setTargetDepartment(rule.getTargetDepartment().getName());
        return dto;
    }
}
