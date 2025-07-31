package com.telros.telros.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telros.telros.model.ERole;
import com.telros.telros.model.Role;
import com.telros.telros.model.User;
import com.telros.telros.model.UserDetails;
import com.telros.telros.dto.request.LoginRequest;
import com.telros.telros.dto.request.UserDetailsRequest;
import com.telros.telros.repository.RoleRepository;
import com.telros.telros.repository.UserDetailsRepository;
import com.telros.telros.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import lombok.RequiredArgsConstructor;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционный тест для контроллера пользователей
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserControllerIntegrationTest(MockMvc mockMvc,
                                        ObjectMapper objectMapper,
                                        UserRepository userRepository,
                                        RoleRepository roleRepository,
                                        UserDetailsRepository userDetailsRepository,
                                        PasswordEncoder passwordEncoder) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private String adminToken;
    private String userToken;
    private User adminUser;
    private User regularUser;
    private UserDetails adminUserDetails;

    @BeforeEach
    public void setup() throws Exception {
        // Очистка базы данных перед каждым тестом
        userDetailsRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();

        // Создание ролей
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        roleRepository.save(userRole);

        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        roleRepository.save(adminRole);

        // Создание администратора
        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("admin"));
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        adminUser.setRoles(adminRoles);
        adminUser = userRepository.save(adminUser);

        // Создание обычного пользователя
        regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setEmail("user@example.com");
        regularUser.setPassword(passwordEncoder.encode("password"));
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        regularUser.setRoles(userRoles);
        regularUser = userRepository.save(regularUser);

        // Создание детальной информации для администратора
        adminUserDetails = new UserDetails();
        adminUserDetails.setUser(adminUser);
        adminUserDetails.setFirstName("Admin");
        adminUserDetails.setLastName("User");
        adminUserDetails.setMiddleName("Test");
        adminUserDetails.setBirthDate(LocalDate.of(1990, 1, 1));
        adminUserDetails.setPhoneNumber("+7 (999) 123-45-67");
        adminUserDetails = userDetailsRepository.save(adminUserDetails);

        // Получение токенов для тестирования
        adminToken = getAuthToken("admin", "admin");
        userToken = getAuthToken("user", "password");
    }

    /**
     * Получение токена аутентификации
     */
    private String getAuthToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        MvcResult result = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = result.getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(contentAsString).get("token").asText();
    }

    /**
     * Тест получения списка всех пользователей (только для администратора)
     */
    @Test
    public void testGetAllUsers() throws Exception {
        // Администратор может получить список всех пользователей
        mockMvc.perform(get("/api/users")
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username", is("admin")))
                .andExpect(jsonPath("$[0].email", is("admin@example.com")))
                .andExpect(jsonPath("$[0].userDetails.firstName", is("Admin")))
                .andExpect(jsonPath("$[0].userDetails.lastName", is("User")));

        // Обычный пользователь не может получить список всех пользователей
        mockMvc.perform(get("/api/users")
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());
    }

    /**
     * Тест получения детальной информации о пользователе по ID
     */
    @Test
    public void testGetUserDetails() throws Exception {
        // Администратор может получить информацию о любом пользователе
        mockMvc.perform(get("/api/users/" + adminUserDetails.getId())
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Admin")))
                .andExpect(jsonPath("$.lastName", is("User")));

        // Обычный пользователь также может получить информацию о пользователе
        mockMvc.perform(get("/api/users/" + adminUserDetails.getId())
                .header("Authorization", userToken))
                .andExpect(status().isOk());
    }

    /**
     * Тест создания/обновления детальной информации о текущем пользователе
     */
    @Test
    public void testCreateOrUpdateCurrentUserDetails() throws Exception {
        UserDetailsRequest userDetailsRequest = new UserDetailsRequest();
        userDetailsRequest.setFirstName("Updated");
        userDetailsRequest.setLastName("User");
        userDetailsRequest.setMiddleName("Test");
        userDetailsRequest.setBirthDate(LocalDate.of(1995, 5, 15));
        userDetailsRequest.setPhoneNumber("+7 (999) 987-65-43");

        ResultActions result = mockMvc.perform(post("/api/users/me")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDetailsRequest)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("User")))
                .andExpect(jsonPath("$.phoneNumber", is("+7 (999) 987-65-43")));
    }

    /**
     * Тест загрузки фотографии для текущего пользователя
     */
    @Test
    public void testUploadCurrentUserPhoto() throws Exception {
        // Сначала создаем детальную информацию для пользователя
        UserDetailsRequest userDetailsRequest = new UserDetailsRequest();
        userDetailsRequest.setFirstName("Photo");
        userDetailsRequest.setLastName("User");
        userDetailsRequest.setMiddleName("Test");
        userDetailsRequest.setBirthDate(LocalDate.of(1995, 5, 15));
        userDetailsRequest.setPhoneNumber("+7 (999) 987-65-43");

        mockMvc.perform(post("/api/users/me")
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDetailsRequest)))
                .andExpect(status().isOk());

        // Затем загружаем фотографию
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes());

        mockMvc.perform(multipart("/api/users/me/photo")
                .file(file)
                .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Фотография успешно загружена")));
    }

    /**
     * Тест удаления пользователя (только для администратора)
     */
    @Test
    public void testDeleteUser() throws Exception {
        // Создаем пользователя для удаления
        UserDetails userToDelete = new UserDetails();
        userToDelete.setUser(regularUser);
        userToDelete.setFirstName("Delete");
        userToDelete.setLastName("User");
        userToDelete = userDetailsRepository.save(userToDelete);

        // Обычный пользователь не может удалить пользователя
        mockMvc.perform(delete("/api/users/" + userToDelete.getId())
                .header("Authorization", userToken))
                .andExpect(status().isForbidden());

        // Администратор может удалить пользователя
        mockMvc.perform(delete("/api/users/" + userToDelete.getId())
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Пользователь успешно удален")));
    }
    
    /**
     * Тест обновления детальной информации о пользователе по ID (только для администратора)
     */
    @Test
    public void testUpdateUserDetails() throws Exception {
        // Создаем пользователя для обновления
        UserDetails userToUpdate = new UserDetails();
        userToUpdate.setUser(regularUser);
        userToUpdate.setFirstName("Original");
        userToUpdate.setLastName("User");
        userToUpdate.setMiddleName("Test");
        userToUpdate.setBirthDate(LocalDate.of(1990, 1, 1));
        userToUpdate.setPhoneNumber("+7 (999) 111-22-33");
        userToUpdate = userDetailsRepository.save(userToUpdate);
        
        UserDetailsRequest updateRequest = new UserDetailsRequest();
        updateRequest.setFirstName("Updated");
        updateRequest.setLastName("UserByAdmin");
        updateRequest.setMiddleName("AdminTest");
        updateRequest.setBirthDate(LocalDate.of(1995, 5, 15));
        updateRequest.setPhoneNumber("+7 (999) 444-55-66");
        
        // Обычный пользователь не может обновить информацию о другом пользователе
        mockMvc.perform(put("/api/users/" + userToUpdate.getId())
                .header("Authorization", userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
        
        // Администратор может обновить информацию о любом пользователе
        mockMvc.perform(put("/api/users/" + userToUpdate.getId())
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("UserByAdmin")))
                .andExpect(jsonPath("$.middleName", is("AdminTest")))
                .andExpect(jsonPath("$.phoneNumber", is("+7 (999) 444-55-66")));
        
        // Проверяем, что данные действительно обновились в базе
        mockMvc.perform(get("/api/users/" + userToUpdate.getId())
                .header("Authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Updated")))
                .andExpect(jsonPath("$.lastName", is("UserByAdmin")));
    }
}