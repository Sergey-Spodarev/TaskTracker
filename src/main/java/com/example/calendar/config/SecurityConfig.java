package com.example.calendar.config;
//Конфигурационные классы (например, настройка безопасности SecurityConfig).

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration //класс содержит конфигурацию Spring.
@EnableWebSecurity //включает безопасность для веб-приложения.
public class SecurityConfig {
    @Bean //регистрирует компонент в Spring-контексте.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll()        // Главная доступна всем
                        .requestMatchers("/profile").authenticated() // Профиль - только авторизованным
                        .requestMatchers("/admin").hasRole("ADMIN")  // Админка - только админам
                )
                .formLogin(form -> form
                        .loginPage("/login")  // Если нужна кастомная страница логина
                        .permitAll()
                );

        return http.build();
    }
}
