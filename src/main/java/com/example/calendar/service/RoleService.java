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

    public List<RoleDTO> findAllRoles(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // ИСПРАВЛЕНО: используем правильный метод
        List<Role> allRoleCompany = roleRepository.findByDepartment_Company(user.getCompany());
        return allRoleCompany.stream()
                .map(this::convertRole)
                .toList();
    }

    public RoleDTO addRole(RoleDTO roleDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        Company company = user.getCompany();

        // ИСПРАВЛЕНО: правильный метод проверки
        boolean flag = roleRepository.existsByCodeAndDepartment_Company(roleDTO.getCode(), company);

        if (flag){
            throw new IllegalArgumentException("The role is already there");
        }
        else{
            Role newRole = new Role();
            newRole.setCode(roleDTO.getCode());

            // TODO: Проверить, может ли пользователь создавать роль для другого департамента
            // Сейчас роль создается для департамента текущего пользователя
            // Если нужно разрешить выбирать департамент - передавать его в DTO
            newRole.setDepartment(user.getDepartment());
            newRole.setDisplayName(roleDTO.getDisplayName());
            return convertRole(roleRepository.save(newRole));
        }
    }

    public RoleDTO updateRole(RoleDTO roleDTO, String userName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();

        // TODO: Проверить права админа
        if(!"ADMIN".equals(admin.getRole().getCode())){
            throw new AccessDeniedException("The user is not admin");
        }

        User user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // ИСПРАВЛЕНО: правильный метод поиска
        Role newRole = roleRepository.findByCodeAndDepartment_Company(roleDTO.getCode(), user.getCompany())
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

        // TODO: Проверить права админа
        if(!"ADMIN".equals(admin.getRole().getCode())){
            throw new AccessDeniedException("The user is not admin");
        }

        // TODO: Проверить, что роль из той же компании
        if (!role.getDepartment().getCompany().equals(admin.getCompany())) {
            throw new AccessDeniedException("You can't change a role from another company.");
        }

        roleRepository.deleteById(roleId);
    }

    public Role getRoleByCodeAndCompany(String code, Company company) {
        // ИСПРАВЛЕНО: правильный метод поиска
        return roleRepository.findByCodeAndDepartment_Company(code, company)
                .orElseThrow(() -> new UsernameNotFoundException("Роль не найдена"));
    }

    private RoleDTO convertRole(Role role){
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setCode(role.getCode());
        roleDTO.setDisplayName(role.getDisplayName());
        return roleDTO;
    }
}