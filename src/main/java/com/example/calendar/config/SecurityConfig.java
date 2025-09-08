package com.example.calendar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.config.http.SessionCreationPolicy;

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
                // Полностью открыты — без входа
                .requestMatchers(
                        "/",
                        "/login",
                        "/api/users/login",
                        "/api/users/register",
                        "/forgot-password",
                        "/reset-password",
                        "/api/companies/register",
                        "/lander",           // ← для приглашённых
                        "/register", // ← форма завершения регистрации
                        "/InvitationToken/completeRegistration"
                ).permitAll()

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
                .hasRole("ADMIN")  // ← только админ

                // === ПРОЕКТЫ ===
                .requestMatchers("/project/create", "/project/update", "/project/{id}", "/project/delete/{id}")
                .hasRole("ADMIN")
                .requestMatchers("/project/getAll", "/project/{id}").authenticated()

                // === ОТДЕЛЫ ===
                .requestMatchers("/department/addDepartment", "/department/updateName", "/department/{department_id}")
                .hasRole("ADMIN")
                .requestMatchers("/department/get").authenticated()

                // === ПРИГЛАШЕНИЯ ===
                .requestMatchers("/InvitationToken/addInvitationToken").hasRole("ADMIN")

                // === КОМПАНИЯ ===
                .requestMatchers("/api/companies/register").permitAll()
                .requestMatchers("/api/companies/update").hasRole("ADMIN")

                // === ПРОФИЛЬ ===
                .requestMatchers("/profile/**").authenticated()

                // ВСЁ ОСТАЛЬНОЕ — только для авторизованных
                .anyRequest().authenticated()
        );

        // Настройка формы входа
        http.formLogin(login -> login
                .loginPage("/")                    // GET / → показываем форму
                .loginProcessingUrl("/login")      // POST /login → обработка
                .defaultSuccessUrl("/task", true)  // после входа — /task
                .failureUrl("/?error=true")        // ошибка — остаёмся на /
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