package com.example.calendar.service;

import com.example.calendar.DTO.UserDTO;
import com.example.calendar.model.Company;
import com.example.calendar.model.Role;
import com.example.calendar.model.User;
import com.example.calendar.repository.CompanyRepository;
import com.example.calendar.repository.RoleRepository;
import com.example.calendar.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//Бизнес-логика (например, UserService для регистрации).
@Service // Помечает класс как сервисный компонент (логика приложения)
@Transactional //Управляет транзакциями для работы с БД.
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, CompanyRepository companyRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
    }

    public Boolean checkUserRegistered(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    public UserDTO registerUser(UserDTO userDTO){
        if (checkUserRegistered(userDTO.getEmail())){
            throw new RuntimeException("Пользователь уже существует");//временно так потом надо сообщение пользователю выбивать
        }

        User user = new User();
        user.setUserName(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        Company company = companyRepository.findByINN((long)111)//todo надо сделать чтобы была проверка есть ли компания или нет
                .orElseThrow(() -> new RuntimeException("компания не найдена"));
        user.setCompany(company);

        Role defaultRole = roleRepository.findByCode("USER")
                .orElseGet(() -> createDefaultUserRole(company));
        user.setRole(defaultRole);
        return convertUserToDTO(userRepository.save(user));
    }

    private Role createDefaultUserRole(Company company) {
        Role role = new Role();
        role.setCode("AWAITING");
        role.setDisplayName("Пользователь");
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
        User user = userRepository.findByEmail(userDTO.getUserName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email:" + userDTO.getEmail()));
        user.setUserName(userDTO.getUserName());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return convertUserToDTO(userRepository.save(user));
    }
}
