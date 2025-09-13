package com.example.calendar.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Отключаем CSRF (временно, для разработки)
        http.csrf(csrf -> csrf.disable());

        // Управление сессиями
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        );

        // Хранилище SecurityContext в сессии
        http.securityContext(c ->
                c.securityContextRepository(new HttpSessionSecurityContextRepository())
        );

        // Настройка авторизации
        http.authorizeHttpRequests(auth -> auth
                // === ПОЛНОСТЬЮ ОТКРЫТЫЕ МАРШРУТЫ ===
                .requestMatchers(
                        "/",
                        "/login",
                        "/api/users/login",
                        "/api/users/register",
                        "/forgot-password",
                        "/reset-password",
                        "/api/companies/register",
                        "/lander",
                        "/register",
                        "/InvitationToken/completeRegistration"
                ).permitAll()

                // === АДМИН-ПАНЕЛИ (HTML-страницы и API) ===
                .requestMatchers("/admin/**").hasRole("ADMIN")  // ← Все пути, начинающиеся с /admin — только для ADMIN

                // === ПРОСТОЙ ДОСТУП К ЗАДАЧАМ (HTML-страница) ===
                .requestMatchers("/task").authenticated()  // ← Любой залогиненный пользователь может зайти на /task

                // === API ЗАДАЧ ===
                .requestMatchers(
                        "/api/v1/task/create",
                        "/api/v1/task/my",
                        "/api/v1/task/created-by-me",
                        "/api/v1/task/getAll",
                        "/api/v1/task/project/{projectId}",
                        "/api/v1/task/{taskId}/subtasks",
                        "/api/v1/task/{taskId}/assignee",
                        "/api/v1/task/{taskId}/status",
                        "/api/v1/task/{taskId}/endTime",
                        "/api/v1/task/{taskId}/title",
                        "/api/v1/task/{taskId}/priority",
                        "/api/v1/task/{taskId}/delete"
                ).authenticated()

                .requestMatchers("/api/v1/task/{parentTaskId}/parentTask").authenticated()
                .requestMatchers("/api/v1/task/{taskId}/history").authenticated()

                // === КОММЕНТАРИИ ===
                .requestMatchers("/api/v1/task/{taskId}/comments").authenticated()
                .requestMatchers("/api/v1/task/{taskId}/comments/{commentId}").authenticated()

                // === РОЛИ ===
                .requestMatchers("/role/all", "/role/add", "/role/updateRole", "/role/{roleId}")
                .hasRole("ADMIN")

                // === ПРОЕКТЫ ===
                .requestMatchers(
                        "/project/create",
                        "/project/update",
                        "/project/{id}",
                        "/project/delete/{id}"
                ).hasRole("ADMIN")
                .requestMatchers("/project/getAll", "/project/{id}").authenticated()

                // === ОТДЕЛЫ ===
                .requestMatchers(
                        "/department/addDepartment",
                        "/department/updateName",
                        "/department/{department_id}"
                ).hasRole("ADMIN")
                .requestMatchers("/department/get").authenticated()

                // === ПРИГЛАШЕНИЯ ===
                .requestMatchers("/InvitationToken/addInvitationToken").hasRole("ADMIN")

                //  доступ к ожидающим пользователям
                .requestMatchers("/api/users/{companyId}/awaiting").hasRole("ADMIN")

                // назначение пользователя в департамент
                .requestMatchers("/api/v1/department/{userId}/assign-role-and-department").hasRole("ADMIN")

                .requestMatchers("/api/v1/department/{userId}/assign-department").hasRole("ADMIN")

                .requestMatchers("/api/users/all").hasRole("ADMIN")

                // === КОМПАНИЯ ===
                .requestMatchers("/api/companies/update").hasRole("ADMIN")

                // === ПРОФИЛЬ ===
                .requestMatchers("/profile/**").authenticated()

                // ВСЁ ОСТАЛЬНОЕ — только для авторизованных
                .anyRequest().authenticated()
        );

        // Настройка формы входа
        http.formLogin(login -> login
                .loginPage("/")
                .loginProcessingUrl("/login")
                .successHandler(new AuthenticationSuccessHandler() {
                    @Override
                    public void onAuthenticationSuccess(HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        Authentication authentication) throws IOException {
                        // Проверяем, есть ли у пользователя роль ADMIN
                        boolean isAdmin = authentication.getAuthorities().stream()
                                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

                        if (isAdmin) {
                            response.sendRedirect("/admin/tasks");
                        } else {
                            response.sendRedirect("/task");
                        }
                    }
                })
                .failureUrl("/?error=true")
        );

        // Настройка выхода
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
        );

        return http.build();
    }

    // Кодировщик паролей
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Менеджер аутентификации
    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}