package com.example.calendar.service;

import com.example.calendar.DTO.CompanyRegistrationDTO;
import com.example.calendar.DTO.RoleDTO;
import com.example.calendar.DTO.UserDTO;
import com.example.calendar.DTO.UserWithRoleDTO;
import com.example.calendar.model.Company;
import com.example.calendar.model.Department;
import com.example.calendar.model.Role;
import com.example.calendar.model.User;
import com.example.calendar.repository.CompanyRepository;
import com.example.calendar.repository.DepartmentRepository;
import com.example.calendar.repository.RoleRepository;
import com.example.calendar.repository.UserRepository;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//Бизнес-логика (например, UserService для регистрации).
@Service // Помечает класс как сервисный компонент (логика приложения)
@Transactional //Управляет транзакциями для работы с БД.
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
    }

    public UserDTO assignUser(Long userId, String codeRole, String nameDepartment){//это когда админ будет назначать пользователю как отдел так и роль
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();
        if (!"ADMIN".equals(admin.getRole().getCode())) {
            throw new RuntimeException("Только администратор может назначать роль пользователю");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!user.getCompany().equals(admin.getCompany())) {
            throw new RuntimeException("Можно назначать роль пользователю только внутри своей компании");
        }

        Role role = roleRepository.findByCodeAndCompany(codeRole, admin.getCompany())
                .orElseThrow(() -> new RuntimeException(codeRole + " роли не существует в данной компании"));

        Department department = departmentRepository.findByNameAndCompany(nameDepartment, admin.getCompany())
                        .orElseThrow(() -> new RuntimeException("Название данного департамента не существует в данной компании"));

        user.setRole(role);
        user.setDepartment(department);
        return convertUserToDTO(userRepository.save(user));
    }

    public void registerCompanyAdmin(Company company, CompanyRegistrationDTO companyRegistrationDTO){
        User admin = new User();
        admin.setUserName(companyRegistrationDTO.getAdminName());
        admin.setEmail(companyRegistrationDTO.getAdminEmail());
        admin.setPassword(passwordEncoder.encode(companyRegistrationDTO.getAdminPassword()));
        admin.setCompany(company);
        admin.setRole(createAdminRole(company));
        userRepository.save(admin);
    }

    private Role createAdminRole(Company company) {
        Role role = new Role();
        role.setCode("ADMIN");
        role.setDisplayName("Администратор");
        role.setCompany(company);
        return roleRepository.save(role);
    }

    public User createPendingUser(String email){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();

        Role role = getDefaultUserRole(admin.getCompany());

        User user = new User();
        user.setEmail(email);
        user.setRole(role);
        user.setCompany(admin.getCompany());
        return userRepository.save(user);
    }

    public UserWithRoleDTO getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        return convertToDTO(user);
    }

    public UserWithRoleDTO convertToDTO(User user) {
        UserWithRoleDTO dto = new UserWithRoleDTO();
        dto.setId(user.getUserId());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());

        if (user.getCompany() != null) {
            dto.setCompanyId(user.getCompany().getId());
        }

        if (user.getDepartment() != null) {
            dto.setDepartmentId(user.getDepartment().getId());
        }

        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
            dto.setRoleName(user.getRole().getDisplayName());
            dto.setRoleCode(user.getRole().getCode());
        }

        return dto;
    }

    private Role getDefaultUserRole(Company company) {
        return roleRepository.findByCodeAndCompany("AWAITING", company)
                .orElseGet(() -> createAndSaveAwaitingRole(company));
    }

    private Role createAndSaveAwaitingRole(Company company) {
        Role role = new Role();
        role.setCode("AWAITING");
        role.setDisplayName("Ожидает регистрации");
        role.setCompany(company);
        return roleRepository.save(role);
    }

    public UserDTO convertUserToDTO(User user){
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName(user.getUserName());
        userDTO.setEmail(user.getEmail());
        userDTO.setPassword(user.getPassword());
        return userDTO;
    }

    public UserDTO updateUser(UserDTO userDTO){
        User user = userRepository.findByEmail(userDTO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email:" + userDTO.getEmail()));
        user.setUserName(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return convertUserToDTO(userRepository.save(user));
    }

    public List<UserWithRoleDTO> getAwaitingRole(Long companyId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User admin = userDetails.getUser();

        if (!admin.getRole().getCode().equals("ADMIN")) {
            throw new AccessDeniedException("Только администратор может получить пользователей");
        }
        if (!admin.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException("Вы не можете смотреть пользователей из другой компании");
        }

        Company company = admin.getCompany();
        Role awaitingRole = roleRepository.findByCodeAndCompany("AWAITING", company)
                .orElseThrow(() -> new RuntimeException("В вашей компании нет людей с ролью Awaiting"));

        if (awaitingRole == null) {
            return List.of();
        }
        List<User> users = userRepository.findByRoleAndCompany(awaitingRole, company);
        return users.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public User getUserById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    public void saveUser(User user){
        userRepository.save(user);
    }
}
