package com.example.calendar.service;

import com.example.calendar.DTO.DepartmentDTO;
import com.example.calendar.model.Department;
import com.example.calendar.model.User;
import com.example.calendar.repository.DepartmentRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    public DepartmentService(DepartmentRepository departmentRepository, UserService userService) {
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    public DepartmentDTO createDepartment(DepartmentDTO departmentDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();

        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new UsernameNotFoundException("Only the administrator can add a new department.");
        }
        if (departmentRepository.existsByCompanyAndName(admin.getCompany(), departmentDTO.getName())) {
            throw new UsernameNotFoundException("This department name already exists.");
        }

        Department department = new Department();
        department.setName(departmentDTO.getName());
        department.setCompany(admin.getCompany());
        return convertToDTO(departmentRepository.save(department));
    }

    public DepartmentDTO updateName(DepartmentDTO departmentDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();

        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new UsernameNotFoundException("Only the administrator can update a new department.");
        }

        Department department = departmentRepository.findById(departmentDTO.getId())
                .orElseThrow(() -> new UsernameNotFoundException("This department does not exist."));
        if (!department.getCompany().equals(admin.getCompany())) {
            throw new AccessDeniedException("Cannot delete department from another company");
        }
        department.setName(departmentDTO.getName());
        return convertToDTO(departmentRepository.save(department));
    }

    public void deleteDepartment(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();

        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new UsernameNotFoundException("Only the administrator can delete a department.");
        }
        if (!departmentRepository.existsById(id)) {
            throw new UsernameNotFoundException("This department does not exist.");
        }

        departmentRepository.deleteById(id);
    }

    public List<DepartmentDTO> getAllDepartments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();

        List<Department> departments = departmentRepository.findByCompany(admin.getCompany());
        return departments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public String assignUserToDepartment(Long userId, Long departmentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();

        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new UsernameNotFoundException("Только администратор может назначить департамент");
        }
        User user = userService.getUserById(userId);
        if (!admin.getCompany().getId().equals(user.getCompany().getId())) {
            throw new AccessDeniedException("Вы не можете назначить роль человеку из другой компании");
        }
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new UsernameNotFoundException("Данный департамент не был найден"));

        user.setDepartment(department);
        userService.saveUser(user);
        return user.getUserName() + "назначен в департамент";
    }

    public DepartmentDTO convertToDTO(Department department) {
        DepartmentDTO departmentDTO = new DepartmentDTO();
        departmentDTO.setId(department.getId());
        departmentDTO.setName(department.getName());
        return departmentDTO;
    }
}
