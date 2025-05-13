package com.example.calendar;


import com.example.calendar.DTO.LoginDto;
import com.example.calendar.controller.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

//@WebMvcTest — загружает только слой MVC (только контроллеры).
//MockMvc — позволяет делать "псевдо" HTTP-запросы без запуска сервера.
//@MockBean — мокаем зависимости (сервисы, репозитории и т.д.).
@WebMvcTest(AuthController.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthController authController;

    @Test
    @WithMockUser
    void testLogin_withCsrfToken() throws Exception {
        // 1. Делаем GET-запрос, чтобы получить CSRF-токен
        MvcResult mvcResult = mockMvc.perform(get("/api/users/login"))
                .andExpect(status().isNotFound()) // можно изменить на isOk(), если нужен конкретный путь
                .andReturn();

        // 2. Получаем сессию, где хранится CSRF-токен
        var session = mvcResult.getRequest().getSession();
        var csrfToken = session.getAttribute("org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN");

        if (csrfToken == null) {
            throw new RuntimeException("CSRF token not found in session");
        }

        // 3. Подготавливаем данные для логина
        LoginDto loginDto = new LoginDto();
        loginDto.setUserName("username");
        loginDto.setPassword("password");

        // 4. Мокаем поведение authenticationManager
        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("username", "password", authorities);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        // 5. Делаем POST-запрос с CSRF-токеном в заголовке
        mockMvc.perform(post("/api/users/login")
                .contentType("application/json")
                        .header("X-CSRF-TOKEN", csrfToken.toString()) // передаём токен
                        .content("{\"userName\":\"username\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("Вы успешно вошли"));

        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testLogin() throws Exception {
        // Подготавливаем DTO
        LoginDto loginDto = new LoginDto();
        loginDto.setUserName("username");
        loginDto.setPassword("password");

        // Подготавливаем мок-результат для authenticate
        var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        var authentication = new UsernamePasswordAuthenticationToken("username", "password", authorities);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        // Делаем запрос
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userName\":\"username\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("username"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("Вы успешно вошли"));

        // Проверяем, что authenticate вызвался один раз
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void testLogin_success() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUserName("username");
        loginDto.setPassword("password");

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        Authentication authentication = new UsernamePasswordAuthenticationToken("username", "password", authorities);

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.message").value("Вы успешно вошли"));
    }
}
