package com.example.calendar.config;
//Конфигурационные классы (например, настройка безопасности SecurityConfig).
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

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

        //Авторизация
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/api/users/login", "/api/users/register",
                        "/forgot-password", "/reset-password", "/api/companies/register").permitAll()

                // API задач
                .requestMatchers("/api/v1/task/create",
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
                        "/api/v1/task/{taskId}/delete").authenticated()

                .requestMatchers("/api/v1/task/{parentTaskId}/parentTask").authenticated()

                // История задач
                .requestMatchers("/api/v1/task/{taskId}/history").authenticated()

                // Комментарии
                .requestMatchers("/api/v1/task/{taskId}/comments").authenticated()
                .requestMatchers("/api/v1/task/{taskId}/comments/{commentId}").authenticated()

                // Роли
                .requestMatchers("/role/all", "/role/add", "/role/updateRole", "/role/{roleId}").authenticated()
                .requestMatchers("/role/all", "/role/add", "/role/updateRole", "/role/{roleId}").hasRole("ADMIN")

                // Проекты
                .requestMatchers("/project/create", "/project/update", "/project/{id}", "/project/delete/{id}").hasRole("ADMIN")
                .requestMatchers("/project/getAll", "/project/{id}").authenticated()

                // Отделы
                .requestMatchers("/department/addDepartment", "/department/updateName", "/department/{department_id}").hasRole("ADMIN")
                .requestMatchers("/department/get").authenticated()

                // Приглашения
                .requestMatchers("/InvitationToken/addInvitationToken").hasRole("ADMIN")
                .requestMatchers("/InvitationToken/completeRegistration").authenticated()

                // Компания
                .requestMatchers("/api/companies/register").permitAll()
                .requestMatchers("/api/companies/update").hasRole("ADMIN")

                // Профиль
                .requestMatchers("/profile/**").authenticated()

                // ВСЁ ОСТАЛЬНОЕ
                .anyRequest().authenticated()
        );

        http.formLogin(login -> login
                .loginPage("/")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/task", true)
                .failureUrl("/?error=true")
        );

        // Выход
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
        );

        return http.build();
    }

    //раскодирует пароль
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Использует DaoAuthenticationProvider для проверки логина/пароля через БД
    //Настройка провайдера с помощью UserDetailsService и PasswordEncoder
    //Возвращает готовый AuthenticationManager, который можно использовать в контроллерах
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