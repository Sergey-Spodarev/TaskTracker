package com.example.calendar.security;

import com.example.calendar.model.Role;
import com.example.calendar.model.User;
import com.example.calendar.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username){
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        System.out.println("\n\n\n\nЭто пользователь = " + user);

        Set<GrantedAuthority> authorities = new HashSet<>();
        Role userRole = user.getRole();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole.getCode()));

        return new CustomUserDetails(user, authorities);
    }
}
