package com.telros.telros.controller;

import com.telros.telros.model.ERole;
import com.telros.telros.model.Role;
import com.telros.telros.model.User;
import com.telros.telros.dto.request.LoginRequest;
import com.telros.telros.dto.request.SignupRequest;
import com.telros.telros.dto.response.JwtResponse;
import com.telros.telros.dto.response.MessageResponse;
import com.telros.telros.repository.RoleRepository;
import com.telros.telros.repository.UserRepository;
import com.telros.telros.security.UserDetailsImpl;
import com.telros.telros.security.jwt.JwtUtils;
import com.telros.telros.model.UserDetails;
import com.telros.telros.repository.UserDetailsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Контроллер для аутентификации и регистрации пользователей
 */
@CrossOrigin(origins = "*", maxAge = 3600) //Настроить CORS перед продакшеном на наш домен
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "API для аутентификации и регистрации пользователей")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final UserDetailsRepository userDetailsRepository;

    /**
     * Аутентификация пользователя
     *
     * @param loginRequest данные для входа
     * @return JWT токен и информация о пользователе
     */
    @Operation(summary = "Аутентификация пользователя", description = "Аутентификация пользователя по логину и паролю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class))}),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные",
                    content = @Content)
    })
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Попытка аутентификации пользователя: {}", loginRequest.getUsername());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("Пользователь {} успешно аутентифицирован", loginRequest.getUsername());
        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    /**
     * Регистрация нового пользователя
     *
     * @param signUpRequest данные для регистрации
     * @return сообщение о результате регистрации
     */
    @Operation(summary = "Регистрация пользователя", description = "Регистрация нового пользователя в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации или пользователь уже существует",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))})
    })
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        log.info("Попытка регистрации нового пользователя: {}", signUpRequest.getUsername());
        
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            log.warn("Регистрация не удалась: имя пользователя {} уже занято", signUpRequest.getUsername());
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Ошибка: Имя пользователя уже занято!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            log.warn("Регистрация не удалась: email {} уже используется", signUpRequest.getEmail());
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Ошибка: Email уже используется!"));
        }

        // Создаем нового пользователя
        User user = new User(signUpRequest.getUsername(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getEmail());

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            log.debug("Роли не указаны, назначается роль по умолчанию: ROLE_USER");
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> {
                        log.error("Роль ROLE_USER не найдена в базе данных");
                        return new RuntimeException("Ошибка: Роль не найдена.");
                    });
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                log.debug("Обработка роли: {}", role);
				if (role.equals("admin")) {
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> {
								log.error("Роль ROLE_ADMIN не найдена в базе данных");
								return new RuntimeException("Ошибка: Роль не найдена.");
							});
					roles.add(adminRole);
				} else {
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> {
								log.error("Роль ROLE_USER не найдена в базе данных");
								return new RuntimeException("Ошибка: Роль не найдена.");
							});
					roles.add(userRole);
				}
            });
        }

        user.setRoles(roles);

        // Создаем и сохраняем пустую детальную информацию
        UserDetails userDetails = new UserDetails();
        userDetails.setUser(user);
		userDetails.setFirstName(signUpRequest.getFirstName());
		userDetails.setLastName(signUpRequest.getLastName());
		user.setUserDetails(userDetails);
		userRepository.save(user);
        log.info("Пользователь {} успешно сохранен в базе данных", user.getUsername());

        log.info("Пользователь {} успешно зарегистрирован", signUpRequest.getUsername());
        return ResponseEntity.ok(new MessageResponse("Пользователь успешно зарегистрирован!"));
    }
}