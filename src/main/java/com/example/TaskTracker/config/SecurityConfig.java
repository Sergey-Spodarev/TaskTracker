package com.example.TaskTracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Настройка CSRF
        http.csrf((csrf) -> csrf.disable());

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        );
        http.securityContext(c ->
                c.securityContextRepository(new HttpSessionSecurityContextRepository())
        );
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/","/login", "/api/users/register/admin", "/api/users/register/user", "/api/admin/**", "/api/users/test").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            String redirectUrl = "/"; // дефолтный путь

                            if (authentication.getAuthorities().stream()
                                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"))) {
                                redirectUrl = "/admin";
                            } else if (authentication.getAuthorities().stream()
                                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER"))) {
                                redirectUrl = "/user";
                            }

                            response.sendRedirect(redirectUrl);
                        })
                        .failureHandler((request, response, ex) -> response.setStatus(HttpStatus.UNAUTHORIZED.value()))
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
