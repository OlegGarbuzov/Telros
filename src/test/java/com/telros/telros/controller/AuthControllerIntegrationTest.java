package com.telros.telros.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telros.telros.model.ERole;
import com.telros.telros.model.Role;
import com.telros.telros.dto.request.LoginRequest;
import com.telros.telros.dto.request.SignupRequest;
import com.telros.telros.repository.RoleRepository;
import com.telros.telros.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционный тест для контроллера аутентификации
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthControllerIntegrationTest(MockMvc mockMvc, ObjectMapper objectMapper,
                                         UserRepository userRepository, RoleRepository roleRepository,
                                         PasswordEncoder passwordEncoder) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeEach
    public void setup() {
        // Очистка базы данных перед каждым тестом
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Создание ролей
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        roleRepository.save(userRole);

        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        roleRepository.save(adminRole);
    }

    /**
     * Тест регистрации нового пользователя
     */
    @Test
    public void testRegisterUser() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setFirstName("testuser");
        signupRequest.setLastName("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password");
        Set<String> roles = new HashSet<>();
        roles.add("user");
        signupRequest.setRole(roles);

        ResultActions result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Пользователь успешно зарегистрирован!")));
    }

    /**
     * Тест регистрации пользователя с уже существующим именем пользователя
     */
    @Test
    public void testRegisterUserWithExistingUsername() throws Exception {
        // Создание пользователя с тем же именем пользователя
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("existinguser");
        signupRequest.setEmail("existing@example.com");
        signupRequest.setPassword("password");
        signupRequest.setFirstName("testuser");
        signupRequest.setLastName("testuser");
        Set<String> roles = new HashSet<>();
        roles.add("user");
        signupRequest.setRole(roles);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        // Попытка создать пользователя с тем же именем пользователя
        SignupRequest duplicateRequest = new SignupRequest();
        duplicateRequest.setUsername("existinguser");
        duplicateRequest.setEmail("another@example.com");
        duplicateRequest.setPassword("password");
        duplicateRequest.setFirstName("testuser");
        duplicateRequest.setLastName("testuser");
        duplicateRequest.setRole(roles);

        ResultActions result = mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Ошибка: Имя пользователя уже занято!")));
    }

    /**
     * Тест аутентификации пользователя
     */
    @Test
    public void testAuthenticateUser() throws Exception {
        // Создание пользователя для аутентификации
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("loginuser");
        signupRequest.setEmail("login@example.com");
        signupRequest.setPassword("password");
        signupRequest.setFirstName("testuser");
        signupRequest.setLastName("testuser");
        Set<String> roles = new HashSet<>();
        roles.add("user");
        signupRequest.setRole(roles);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)));

        // Аутентификация пользователя
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("loginuser");
        loginRequest.setPassword("password");

        ResultActions result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username", is("loginuser")))
                .andExpect(jsonPath("$.email", is("login@example.com")));
    }
}