package com.example.calendar.service;

import com.example.calendar.DTO.RoleDTO;
import com.example.calendar.model.Company;
import com.example.calendar.model.Role;
import com.example.calendar.model.User;
import com.example.calendar.repository.RoleRepository;
import com.example.calendar.repository.UserRepository;
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
        User user = (User) authentication.getPrincipal();
        List<Role> allRoleCompany = roleRepository.findByCompany(user.getCompany())
                .orElseThrow(() -> new UsernameNotFoundException("Company not found"));
        return allRoleCompany.stream()
                .map(Role::getDisplayName)
                .toList();
    }

    private RoleDTO convertRole(Role role){
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setCode(role.getCode());
        roleDTO.setDisplayName(role.getDisplayName());
        return roleDTO;
    }
}
