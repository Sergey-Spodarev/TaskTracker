package com.example.calendar.config;
//Конфигурационные классы (например, настройка безопасности SecurityConfig).
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Настройка CSRF
        http.csrf((csrf) -> csrf.disable());

        // Авторизация
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register").permitAll()
                .requestMatchers("/api/**").permitAll() // Все API-маршруты открыты
                .requestMatchers("/profile/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
        );

        // Форма входа
        http.formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/profile")
                .permitAll()
        );

        // Выход
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
        );

        return http.build();
    }
}