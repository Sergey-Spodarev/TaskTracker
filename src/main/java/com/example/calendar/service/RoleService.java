package com.example.calendar.service;

import com.example.calendar.DTO.RoleDTO;
import com.example.calendar.model.Company;
import com.example.calendar.model.Role;
import com.example.calendar.model.User;
import com.example.calendar.repository.RoleRepository;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public RoleService(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public List<String> findAllRoles(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        List<Role> allRoleCompany = roleRepository.findByCompany(user.getCompany())
                .orElseThrow(() -> new UsernameNotFoundException("Company not found"));
        return allRoleCompany.stream()
                .map(Role::getDisplayName)
                .toList();
    }

    public RoleDTO addRole(RoleDTO roleDTO) {//может потом сделать проверку того что этот метод вызывает только админ
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        Company company = user.getCompany();
        boolean flag = roleRepository.existsByCodeAndCompany(roleDTO.getCode(), company);

        if (flag){
            throw new IllegalArgumentException("The role is already there");
        }
        else{
            Role newRole = new Role();
            newRole.setCode(roleDTO.getCode());
            newRole.setCompany(company);
            newRole.setDisplayName(roleDTO.getDisplayName());
            return convertRole(roleRepository.save(newRole));
        }
    }

    public RoleDTO updateRole(RoleDTO roleDTO, String userName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();
        if(!"ADMIN".equals(admin.getRole().getCode())){
            throw new AccessDeniedException("The user is not admin");
        }

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Role newRole = roleRepository.findByCodeAndCompany(user.getCompany(), roleDTO.getCode())
                .orElseThrow(() -> new UsernameNotFoundException("Role not found"));

        user.setRole(newRole);
        userRepository.save(user);

        return convertRole(newRole);
    }

    public void deleteRole(Long roleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new UsernameNotFoundException("Role not found"));

        if(!"ADMIN".equals(admin.getRole().getCode())){
            throw new AccessDeniedException("The user is not admin");
        }
        if (!role.getCompany().equals(admin.getCompany())) {
            throw new AccessDeniedException("You can't change a role from another company.");
        }
        roleRepository.deleteById(roleId);
    }

    private RoleDTO convertRole(Role role){
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setCode(role.getCode());
        roleDTO.setDisplayName(role.getDisplayName());
        return roleDTO;
    }
}