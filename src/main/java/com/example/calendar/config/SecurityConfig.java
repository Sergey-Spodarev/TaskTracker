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

        // Авторизация
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/api/users/login", "/api/users/register", "/forgot-password", "/reset-password", "/api/companies/register").permitAll()
                .requestMatchers("/api/**", "/department/**", "/role/**").authenticated()
                .requestMatchers("/profile/**", "/InvitationToken/**").authenticated()
                .requestMatchers("/admin/**", "/assignmentRuleService/**", "/company/**").hasRole("ADMIN")
                .anyRequest().permitAll()
        );

        http.formLogin(login -> login
                .loginPage("/")           // форма входа — на главной
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/tasks", true)  // ← ВАЖНО: после входа — на /tasks
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