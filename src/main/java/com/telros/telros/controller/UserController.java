package com.telros.telros.controller;

import com.telros.telros.dto.response.UserResponse;
import com.telros.telros.model.UserPhoto;
import com.telros.telros.dto.request.UserDetailsRequest;
import com.telros.telros.dto.response.MessageResponse;
import com.telros.telros.dto.response.UserDetailsResponse;
import com.telros.telros.service.UserPhotoService;
import com.telros.telros.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Контроллер для работы с пользователями и их детальной информацией
 */
@CrossOrigin(origins = "*", maxAge = 3600) //Настроить CORS перед продакшеном на наш домен
@RestController
@RequestMapping("/api/users")
@Tag(name = "Пользователи", description = "API для работы с пользователями и их детальной информацией")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserPhotoService userPhotoService;

    /**
     * Получить список всех пользователей
     *
     * @return список пользователей с детальной информацией
     */
    @Operation(summary = "Получить список всех пользователей", description = "Получить список всех пользователей с их детальной информацией")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список пользователей успешно получен",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Запрос на получение списка всех пользователей");
        List<UserResponse> users = userService.getAllUsers();
        log.info("Список пользователей успешно получен, количество: {}", users.size());
        return ResponseEntity.ok(users);
    }

    /**
     * Получить детальную информацию о пользователе по ID
     *
     * @param id ID пользователя
     * @return детальная информация о пользователе
     */
    @Operation(summary = "Получить детальную информацию о пользователе", description = "Получить детальную информацию о пользователе по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно получена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDetailsResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserDetails(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id) {
        log.info("Запрос на получение пользователя с ID: {}", id);
        try {
            UserDetailsResponse userDetails = userService.getUserDetailsById(id);
            log.info("Пользователь с ID {} успешно найден", id);
            return ResponseEntity.ok(userDetails);
        } catch (EntityNotFoundException e) {
            log.error("Пользователь с ID {} не найден", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Получить детальную информацию о текущем пользователе
     *
     * @return детальная информация о текущем пользователе
     */
    @Operation(summary = "Получить детальную информацию о текущем пользователе", description = "Получить детальную информацию о текущем аутентифицированном пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно получена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDetailsResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Информация о пользователе не найдена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content)
    })
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUserDetails() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Запрос на получение информации о текущем пользователе: {}", username);
            UserDetailsResponse userDetails = userService.getUserDetailsByUsername(username);
            log.info("Информация о текущем пользователе {} успешно получена", username);
            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            log.error("Ошибка при получении информации о текущем пользователе: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Создать или обновить детальную информацию о текущем пользователе
     *
     * @param userDetailsRequest данные для создания/обновления
     * @return обновленная детальная информация о пользователе
     */
    @Operation(summary = "Создать или обновить детальную информацию о текущем пользователе", description = "Создать или обновить детальную информацию о текущем аутентифицированном пользователе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно обновлена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDetailsResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content)
    })
    @PostMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createOrUpdateCurrentUserDetails(
            @Valid @RequestBody UserDetailsRequest userDetailsRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Запрос на создание/обновление информации о текущем пользователе: {}", username);
            UserDetailsResponse userDetails = userService.createOrUpdateUserDetails(username, userDetailsRequest);
            log.info("Информация о пользователе {} успешно обновлена", username);
            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            log.error("Ошибка при обновлении информации о пользователе: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Обновить детальную информацию о пользователе по ID (только для администраторов)
     *
     * @param id                 ID пользователя
     * @param userDetailsRequest данные для обновления
     * @return обновленная детальная информация о пользователе
     */
    @Operation(summary = "Обновить детальную информацию о пользователе", description = "Обновить детальную информацию о пользователе по ID (только для администраторов)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно обновлена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDetailsResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserDetails(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UserDetailsRequest userDetailsRequest) {
        try {
            log.info("Запрос на обновление информации о пользователе с ID: {}", id);
            UserDetailsResponse userDetails = userService.updateUserDetailsById(id, userDetailsRequest);
            log.info("Информация о пользователе с ID {} успешно обновлена", id);
            return ResponseEntity.ok(userDetails);
        } catch (EntityNotFoundException e) {
            log.error("Пользователь с ID {} не найден при попытке обновления: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Ошибка при обновлении информации о пользователе с ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Удалить пользователя по ID (только для администраторов)
     *
     * @param id ID пользователя
     * @return сообщение о результате удаления
     */
    @Operation(summary = "Удалить пользователя", description = "Удалить пользователя по ID (только для администраторов)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удален",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id) {
        try {
            log.info("Запрос на удаление пользователя с ID: {}", id);
            userService.deleteUserDetails(id);
            log.info("Пользователь с ID {} успешно удален", id);
            return ResponseEntity.ok(new MessageResponse("Пользователь успешно удален"));
        } catch (EntityNotFoundException e) {
            log.error("Пользователь с ID {} не найден при попытке удаления: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Получить фотографию пользователя по ID
     *
     * @param id ID пользователя
     * @return фотография пользователя
     */
    @Operation(summary = "Получить фотографию пользователя", description = "Получить фотографию пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотография пользователя успешно получена",
                    content = {@Content(mediaType = "image/*")}),
            @ApiResponse(responseCode = "404", description = "Пользователь или фотография не найдены",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content)
    })
    @GetMapping("/{id}/photo")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserPhoto(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id) {
        try {
            log.info("Запрос на получение фотографии пользователя с ID: {}", id);
            UserPhoto userPhoto = userPhotoService.getUserPhoto(id);
            log.info("Фотография пользователя с ID {} успешно получена", id);
            log.debug("Тип контента: {}, имя файла: {}", userPhoto.getFileType(), userPhoto.getFileName());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + userPhoto.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(userPhoto.getFileType()))
                    .body(userPhoto.getData());
        } catch (EntityNotFoundException e) {
            log.error("Фотография пользователя с ID {} не найдена: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Загрузить фотографию для текущего пользователя
     *
     * @param file файл фотографии
     * @return сообщение о результате загрузки
     */
    @Operation(summary = "Загрузить фотографию для текущего пользователя", description = "Загрузить фотографию для текущего аутентифицированного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотография успешно загружена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Ошибка при загрузке файла",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content)
    })
    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadCurrentUserPhoto(
            @Parameter(description = "Файл фотографии", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                log.warn("Пустой файл при попытке загрузки");
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Файл не выбран или он пустой"));
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Запрос на загрузку фотографии для текущего пользователя: {}", username);
            log.debug("Размер загружаемого файла: {} байт, тип: {}", file.getSize(), file.getContentType());

            UserDetailsResponse userDetails = userService.getUserDetailsByUsername(username);
            userPhotoService.uploadUserPhoto(userDetails.getId(), file);
            log.info("Фотография для пользователя {} успешно загружена", username);
            return ResponseEntity.ok(new MessageResponse("Фотография успешно загружена"));
        } catch (EntityNotFoundException e) {
            log.error("Пользователь не найден при загрузке фотографии: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (IOException e) {
            log.error("Ошибка при загрузке файла: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Ошибка при загрузке файла: " + e.getMessage()));
        }
    }

    /**
     * Загрузить фотографию для пользователя по ID (только для администраторов)
     *
     * @param id   ID пользователя
     * @param file файл фотографии
     * @return сообщение о результате загрузки
     */
    @Operation(summary = "Загрузить фотографию для пользователя", description = "Загрузить фотографию для пользователя по ID (только для администраторов)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотография успешно загружена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Ошибка при загрузке файла",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content)
    })
    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadUserPhoto(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id,
            @Parameter(description = "Файл фотографии", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Файл не выбран или он пустой"));
            }

            userPhotoService.uploadUserPhoto(id, file);
            return ResponseEntity.ok(new MessageResponse("Фотография успешно загружена"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Ошибка при загрузке файла: " + e.getMessage()));
        }
    }

    /**
     * Удалить фотографию пользователя по ID (только для администраторов)
     *
     * @param id ID пользователя
     * @return сообщение о результате удаления
     */
    @Operation(summary = "Удалить фотографию пользователя", description = "Удалить фотографию пользователя по ID (только для администраторов)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотография успешно удалена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content)
    })
    @DeleteMapping("/{id}/photo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserPhoto(
            @Parameter(description = "ID пользователя", required = true)
            @PathVariable Long id) {
        try {
            log.info("Запрос на удаление фотографии пользователя с ID: {}", id);
            userPhotoService.deleteUserPhoto(id);
            log.info("Фотография пользователя с ID {} успешно удалена", id);
            return ResponseEntity.ok(new MessageResponse("Фотография успешно удалена"));
        } catch (EntityNotFoundException e) {
            log.error("Фотография пользователя с ID {} не найдена при попытке удаления: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    /**
     * Удалить фотографию текущего пользователя
     *
     * @return сообщение о результате удаления
     */
    @Operation(summary = "Удалить фотографию текущего пользователя", description = "Удалить фотографию текущего аутентифицированного пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Фотография успешно удалена",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "404", description = "Пользователь или фотография не найдены",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = MessageResponse.class))}),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен",
                    content = @Content)
    })
    @DeleteMapping("/me/photo")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteCurrentUserPhoto() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            log.info("Запрос на удаление фотографии текущего пользователя: {}", username);
            UserDetailsResponse userDetails = userService.getUserDetailsByUsername(username);
            userPhotoService.deleteUserPhoto(userDetails.getId());
            log.info("Фотография пользователя {} успешно удалена", username);
            return ResponseEntity.ok(new MessageResponse("Фотография успешно удалена"));
        } catch (EntityNotFoundException e) {
            log.error("Ошибка при удалении фотографии текущего пользователя: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}