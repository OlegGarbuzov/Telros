package com.telros.telros.service;

import com.telros.telros.dto.response.UserResponse;
import com.telros.telros.mapper.UserMapper;
import com.telros.telros.model.User;
import com.telros.telros.model.UserDetails;
import com.telros.telros.dto.request.UserDetailsRequest;
import com.telros.telros.dto.response.UserDetailsResponse;
import com.telros.telros.repository.UserDetailsRepository;
import com.telros.telros.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с пользователями и их детальной информацией
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final UserMapper userMapper;

    /**
     * Получить список всех пользователей с детальной информацией
     *
     * @return список пользователей с детальной информацией
     */
    public List<UserResponse> getAllUsers() {
        log.info("Получение списка всех пользователей");
        return userRepository.findAll().stream()
                .map(userMapper::userToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получить детальную информацию о пользователе по ID
     *
     * @param id ID пользователя
     * @return детальная информация о пользователе
     * @throws EntityNotFoundException если пользователь не найден
     */
    public UserDetailsResponse getUserDetailsById(Long id) {
        log.info("Получение информации о пользователе с ID: {}", id);
        UserDetails userDetails = userDetailsRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", id);
                    return new EntityNotFoundException("Пользователь с ID " + id + " не найден");
                });
        return userMapper.userDetailsToUserDetailsResponse(userDetails);
    }

    /**
     * Получить детальную информацию о пользователе по имени пользователя
     *
     * @param username имя пользователя
     * @return детальная информация о пользователе
     * @throws UsernameNotFoundException если пользователь не найден
     */
    public UserDetailsResponse getUserDetailsByUsername(String username) {
        log.info("Получение информации о пользователе по имени: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Пользователь с именем {} не найден", username);
                    return new UsernameNotFoundException("Пользователь не найден: " + username);
                });

        UserDetails userDetails = userDetailsRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.error("Детальная информация не найдена для пользователя: {}", username);
                    return new EntityNotFoundException("Детальная информация не найдена для пользователя: " + username);
                });

        return userMapper.userDetailsToUserDetailsResponse(userDetails);
    }

    /**
     * Создать или обновить детальную информацию о пользователе
     *
     * @param username           имя пользователя
     * @param userDetailsRequest данные для создания/обновления
     * @return обновленная детальная информация о пользователе
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Transactional
    public UserDetailsResponse createOrUpdateUserDetails(String username, UserDetailsRequest userDetailsRequest) {
        log.info("Создание/обновление информации о пользователе: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Пользователь с именем {} не найден", username);
                    return new UsernameNotFoundException("Пользователь не найден: " + username);
                });

        UserDetails userDetails = userDetailsRepository.findByUserId(user.getId())
                .orElse(new UserDetails());

        userDetails.setUser(user);
        userMapper.updateUserDetailsFromRequest(userDetailsRequest, userDetails);

        userDetails = userDetailsRepository.save(userDetails);
        log.info("Информация о пользователе {} успешно сохранена", username);

        return userMapper.userDetailsToUserDetailsResponse(userDetails);
    }

    /**
     * Удалить детальную информацию о пользователе
     *
     * @param id ID пользователя
     * @throws EntityNotFoundException если пользователь не найден
     */
    @Transactional
    public void deleteUserDetails(Long id) {
        log.info("Удаление информации о пользователе с ID: {}", id);
        if (!userDetailsRepository.existsById(id)) {
            log.error("Пользователь с ID {} не найден", id);
            throw new EntityNotFoundException("Пользователь с ID " + id + " не найден");
        }
        userDetailsRepository.deleteById(id);
        log.info("Информация о пользователе с ID {} успешно удалена", id);
    }

    /**
     * Обновить детальную информацию о пользователе по ID
     *
     * @param id ID пользователя
     * @param userDetailsRequest данные для обновления
     * @return обновленная детальная информация о пользователе
     * @throws EntityNotFoundException если пользователь не найден
     */
    @Transactional
    public UserDetailsResponse updateUserDetailsById(Long id, UserDetailsRequest userDetailsRequest) {
        log.info("Обновление информации о пользователе с ID: {}", id);
        UserDetails userDetails = userDetailsRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", id);
                    return new EntityNotFoundException("Пользователь с ID " + id + " не найден");
                });

        User user = userDetails.getUser();
        if (user == null) {
            log.error("Для пользователя с ID {} не найдена основная информация", id);
            throw new EntityNotFoundException("Для пользователя с ID " + id + " не найдена основная информация");
        }

        userMapper.updateUserDetailsFromRequest(userDetailsRequest, userDetails);
        userDetails = userDetailsRepository.save(userDetails);
        log.info("Информация о пользователе с ID {} успешно обновлена", id);

        return userMapper.userDetailsToUserDetailsResponse(userDetails);
    }


}