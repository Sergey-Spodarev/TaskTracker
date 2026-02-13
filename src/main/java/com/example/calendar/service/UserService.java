package com.example.calendar.service;

import com.example.calendar.DTO.*;
import com.example.calendar.model.*;
import com.example.calendar.repository.*;
import com.example.calendar.security.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

//Бизнес-логика (например, UserService для регистрации).
@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentLevelRepository departmentLevelRepository;
    private final SchemePermissionService schemePermissionService;
    private final RoleLevelRepository roleLevelRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository,
                       DepartmentRepository departmentRepository,
                       DepartmentLevelRepository departmentLevelRepository,
                       SchemePermissionService schemePermissionService,
                       RoleLevelRepository roleLevelRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.departmentLevelRepository = departmentLevelRepository;
        this.schemePermissionService = schemePermissionService;
        this.roleLevelRepository = roleLevelRepository;
    }

    public UserDTO assignUser(UserAssignmentDTO assignmentDTO) {
        User admin = getCurrentUser();

        if (!schemePermissionService.hasPermission(admin, "assign_user_role")) {
            throw new SecurityException("Недостаточно прав для назначения ролей пользователям");
        }

        User user = userRepository.findById(assignmentDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!user.getCompany().equals(admin.getCompany())) {
            throw new SecurityException("Можно назначать роль пользователю только внутри своей компании");
        }

        Role role = roleRepository.findByCodeAndCompany(assignmentDTO.getRoleCode(), admin.getCompany())
                .orElseThrow(() -> new RuntimeException(assignmentDTO.getRoleCode() + " роли не существует в данной компании"));

        Department department = departmentRepository.findByNameAndCompany(assignmentDTO.getDepartmentName(), admin.getCompany())
                .orElseThrow(() -> new RuntimeException("Название данного департамента не существует в данной компании"));

        if (admin.getSystemRole() == SystemRole.DEPARTMENT_HEAD &&
                !admin.getDepartment().equals(department)) {
            throw new SecurityException("Можно назначать только в своем отделе");
        }

        if (!departmentLevelRepository.existsByDepartmentAndLevel(department, assignmentDTO.getDepartmentLevel())) {
            throw new RuntimeException("В данном департаменте нет такого уровня");
        }

        if (assignmentDTO.getRoleLevelId() != null) {
            RoleLevel roleLevel = roleLevelRepository.findById(assignmentDTO.getRoleLevelId())
                    .orElseThrow(() -> new RuntimeException("Уровень роли не найден"));

            if (!roleLevel.getRole().getId().equals(role.getId())) {
                throw new SecurityException("Уровень роли не соответствует выбранной роли");
            }

            if (!roleLevel.getRole().getDepartment().getCompany().equals(admin.getCompany())) {
                throw new SecurityException("Уровень роли не принадлежит вашей компании");
            }

            user.setRoleLevel(roleLevel);
        } else {
            List<RoleLevel> roleLevels = roleLevelRepository.findAllByCompany(admin.getCompany())
                    .stream()
                    .filter(rl -> rl.getRole().getId().equals(role.getId()))
                    .toList();

            if (!roleLevels.isEmpty()) {
                RoleLevel defaultLevel = roleLevels.stream()
                        .min(Comparator.comparing(RoleLevel::getLevel))
                        .orElse(roleLevels.get(0));
                user.setRoleLevel(defaultLevel);
            }
        }

        user.setRole(role);
        user.setDepartment(department);

        return convertUserToDTO(userRepository.save(user));
    }

    public void registerCompanyAdmin(Company company, CompanyRegistrationDTO companyRegistrationDTO) {
        User admin = new User();
        admin.setUserName(companyRegistrationDTO.getAdminName());
        admin.setEmail(companyRegistrationDTO.getAdminEmail());
        admin.setPassword(passwordEncoder.encode(companyRegistrationDTO.getAdminPassword()));
        admin.setCompany(company);
        admin.setSystemRole(SystemRole.COMPANY_OWNER);

        Role adminRole = createAdminRole(company);
        admin.setRole(adminRole);

        Department defaultDepartment = createDefaultDepartment(company);
        admin.setDepartment(defaultDepartment);

        RoleLevel adminRoleLevel = createAdminRoleLevel(adminRole);
        admin.setRoleLevel(adminRoleLevel);

        userRepository.save(admin);
    }

    private Department createDefaultDepartment(Company company) {
        Department department = new Department();
        department.setName("Администрация");
        department.setCompany(company);
        return departmentRepository.save(department);
    }

    public User createPendingUser(String email) {
        User admin = getCurrentUser();

        if (!schemePermissionService.hasPermission(admin, "invite_user")) {
            throw new SecurityException("Недостаточно прав для приглашения пользователей");
        }

        Role role = getDefaultUserRole(admin.getCompany());

        List<Department> companyDepartments = departmentRepository.findByCompany(admin.getCompany());
        Department defaultDepartment = companyDepartments.isEmpty() ?
                createDefaultDepartment(admin.getCompany()) : companyDepartments.get(0);

        User user = new User();
        user.setEmail(email);
        user.setSystemRole(SystemRole.TEAM_MEMBER);
        user.setRole(role);
        user.setDepartment(defaultDepartment);
        user.setCompany(admin.getCompany());
        return userRepository.save(user);
    }

    public UserWithRoleDTO getUser() {
        User user = getCurrentUser();
        return convertToDTO(user);
    }

    private Role getDefaultUserRole(Company company) {
        return roleRepository.findByCodeAndCompany("AWAITING", company)
                .orElseGet(() -> createAndSaveAwaitingRole(company));
    }

    private Role createAndSaveAwaitingRole(Company company) {
        // Сначала нужно найти или создать дефолтный department для компании
        List<Department> companyDepartments = departmentRepository.findByCompany(company);
        Department defaultDepartment = companyDepartments.isEmpty() ?
                createDefaultDepartment(company) : companyDepartments.get(0);

        Role role = new Role();
        role.setCode("AWAITING");
        role.setDisplayName("Ожидает регистрации");
        role.setDepartment(defaultDepartment); // ← Устанавливаем department, а не company!
        return roleRepository.save(role);
    }

    public UserDTO updateUser(UserDTO userDTO) {
        User currentUser = getCurrentUser();

        if (!currentUser.getId().equals(userDTO.getId())) {
            if (!schemePermissionService.hasPermission(currentUser, "update_other_user")) {
                throw new SecurityException("Недостаточно прав для обновления данных другого пользователя");
            }
        }

        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (!user.getCompany().equals(currentUser.getCompany())) {
            throw new SecurityException("Нельзя обновлять данные пользователя из другой компании");
        }

        user.setUserName(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        return convertUserToDTO(userRepository.save(user));
    }

    public List<UserWithRoleDTO> getAwaitingRole(Long companyId) {
        User admin = getCurrentUser();

        if (!schemePermissionService.hasPermission(admin, "view_users")) {
            throw new SecurityException("Недостаточно прав для просмотра пользователей");
        }

        if (!admin.getCompany().getId().equals(companyId)) {
            throw new SecurityException("Вы не можете смотреть пользователей из другой компании");
        }

        Company company = admin.getCompany();
        Role awaitingRole = roleRepository.findByCodeAndCompany("AWAITING", company)
                .orElseThrow(() -> new RuntimeException("В вашей компании нет людей с ролью Awaiting"));

        List<User> users = userRepository.findByRoleAndCompany(awaitingRole, company);
        return users.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public List<UserDTO> getAllUsers() {
        User user = getCurrentUser();

        if (!schemePermissionService.hasPermission(user, "view_users")) {
            throw new SecurityException("Недостаточно прав для просмотра пользователей");
        }

        List<User> users = userRepository.findByCompany(user.getCompany());
        return users.stream()
                .map(this::convertUserToDTO)
                .toList();
    }

    public User getUserById(Long id) {
        User currentUser = getCurrentUser();

        if (!schemePermissionService.hasPermission(currentUser, "view_user_details")) {
            throw new SecurityException("Недостаточно прав для просмотра данных пользователя");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!user.getCompany().equals(currentUser.getCompany())) {
            throw new SecurityException("Пользователь не принадлежит вашей компании");
        }

        return user;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public UserDTO changeUserDepartment(Long userId, Long newDepartmentId) {
        User admin = getCurrentUser();

        if (!schemePermissionService.hasPermission(admin, "change_user_department")) {
            throw new SecurityException("Недостаточно прав для смены отдела пользователя");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!user.getCompany().equals(admin.getCompany())) {
            throw new SecurityException("Пользователь не принадлежит вашей компании");
        }

        Department newDepartment = departmentRepository.findById(newDepartmentId)
                .orElseThrow(() -> new RuntimeException("Отдел не найден"));

        if (admin.getSystemRole() == SystemRole.DEPARTMENT_HEAD &&
                !admin.getDepartment().equals(newDepartment)) {
            throw new SecurityException("Можно перемещать только в своем отделе");
        }

        user.setDepartment(newDepartment);
        return convertUserToDTO(userRepository.save(user));
    }

    private Role createAdminRole(Company company) {
        // Находим или создаем department для администрации
        Department adminDepartment = departmentRepository.findByNameAndCompany("Администрация", company)
                .orElseGet(() -> {
                    Department dept = new Department();
                    dept.setName("Администрация");
                    dept.setCompany(company);
                    return departmentRepository.save(dept);
                });

        Role role = new Role();
        role.setCode("ADMIN");
        role.setDisplayName("Администратор");
        role.setDepartment(adminDepartment); // ← Устанавливаем department
        return roleRepository.save(role);
    }

    private RoleLevel createAdminRoleLevel(Role role) {
        RoleLevel roleLevel = new RoleLevel();
        roleLevel.setLevel(100);
        roleLevel.setLevelName("Администратор");
        roleLevel.setRole(role);
        return roleLevelRepository.save(roleLevel);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser();
    }

    public UserDTO convertUserToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUserName(user.getUserName());
        userDTO.setEmail(user.getEmail());
        userDTO.setSystemRole(user.getSystemRole());

        if (user.getDepartment() != null) {
            userDTO.setDepartmentId(user.getDepartment().getId());
        }

        if (user.getRoleLevel() != null) {
            userDTO.setRoleLevelId(user.getRoleLevel().getId());
            userDTO.setRoleLevel(user.getRoleLevel().getLevel());
        }

        return userDTO;
    }

    public UserWithRoleDTO convertToDTO(User user) {
        UserWithRoleDTO dto = new UserWithRoleDTO();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setSystemRole(user.getSystemRole());

        if (user.getCompany() != null) {
            dto.setCompanyId(user.getCompany().getId());
        }

        if (user.getDepartment() != null) {
            dto.setDepartmentId(user.getDepartment().getId());
            dto.setDepartmentName(user.getDepartment().getName());
        }

        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
            dto.setRoleName(user.getRole().getDisplayName());
            dto.setRoleCode(user.getRole().getCode());
        }

        if (user.getRoleLevel() != null) {
            dto.setRoleLevelId(user.getRoleLevel().getId());
            dto.setRoleLevel(user.getRoleLevel().getLevel());
            dto.setRoleLevelName(user.getRoleLevel().getLevelName());
        }

        return dto;
    }
}