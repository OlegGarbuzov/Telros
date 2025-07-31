package com.telros.telros.config;

import com.telros.telros.model.ERole;
import com.telros.telros.model.Role;
import com.telros.telros.model.User;
import com.telros.telros.repository.RoleRepository;
import com.telros.telros.repository.UserRepository;
import com.telros.telros.model.UserDetails;
import com.telros.telros.repository.UserDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Класс для инициализации базы данных начальными данными
 */
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsRepository userDetailsRepository;

    @Override
    public void run(String... args) {
        initRoles();
        createDefaultAdmin();
    }

    /**
     * Инициализация ролей в базе данных
     */
    private void initRoles() {
        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName(ERole.ROLE_USER);
            roleRepository.save(userRole);

            Role adminRole = new Role();
            adminRole.setName(ERole.ROLE_ADMIN);
            roleRepository.save(adminRole);

            System.out.println("Роли успешно созданы");
        }
    }

    /**
     * Создание администратора по умолчанию (admin:admin)
     */
    private void createDefaultAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("admin"));

            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Ошибка: Роль ADMIN не найдена."));
            roles.add(adminRole);
            admin.setRoles(roles);

           // Создаем и сохраняем пустую детальную информацию для администратора
           UserDetails adminDetails = new UserDetails();
           adminDetails.setUser(admin);
           adminDetails.setFirstName("Админ");
           adminDetails.setLastName("Админ");
           admin.setUserDetails(adminDetails);
           userRepository.save(admin);

            System.out.println("Администратор по умолчанию успешно создан");
        }
    }
}