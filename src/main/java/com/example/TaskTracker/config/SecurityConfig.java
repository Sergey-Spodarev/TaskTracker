package com.example.TaskTracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/api/users/register").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        //.requestMatchers("/admin/**").hasRole("ADMIN")   // Только для админов
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/api/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")//URL для выхода
                        .logoutSuccessUrl("/")//Перенаправление после выхода
                        .invalidateHttpSession(true)//!!!! Удалить сессию
                        .deleteCookies("JSESSIONID")//!!! Удалить куки сессии
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
