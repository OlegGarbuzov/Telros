package com.telros.telros.service;

import com.telros.telros.model.UserDetails;
import com.telros.telros.model.UserPhoto;
import com.telros.telros.repository.UserDetailsRepository;
import com.telros.telros.repository.UserPhotoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Сервис для работы с фотографиями пользователей
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserPhotoService {

    private final UserPhotoRepository userPhotoRepository;
    private final UserDetailsRepository userDetailsRepository;

    /**
     * Получить фотографию пользователя по ID пользователя
     *
     * @param userDetailsId ID пользователя
     * @return фотография пользователя
     * @throws EntityNotFoundException если пользователь или фотография не найдены
     */
    @Transactional(readOnly = true)
    public UserPhoto getUserPhoto(Long userDetailsId) {
        log.info("Получение фотографии пользователя с ID: {}", userDetailsId);
        if (!userDetailsRepository.existsById(userDetailsId)) {
            log.error("Пользователь с ID {} не найден", userDetailsId);
            throw new EntityNotFoundException("Пользователь с ID " + userDetailsId + " не найден");
        }

        UserPhoto photo = userPhotoRepository.findByUserDetails_Id(userDetailsId)
                .orElseThrow(() -> {
                    log.error("Фотография для пользователя с ID {} не найдена", userDetailsId);
                    return new EntityNotFoundException("Фотография для пользователя с ID " + userDetailsId + " не найдена");
                });
        log.debug("Фотография пользователя с ID {} успешно получена", userDetailsId);
        return photo;
    }

    /**
     * Загрузить или обновить фотографию пользователя
     *
     * @param userDetailsId ID пользователя
     * @param file          файл фотографии
     * @throws EntityNotFoundException если пользователь не найден
     * @throws IOException             если произошла ошибка при чтении файла
     */
    @Transactional
    public void uploadUserPhoto(Long userDetailsId, MultipartFile file) throws IOException {
        log.info("Загрузка фотографии для пользователя с ID: {}", userDetailsId);
        UserDetails userDetails = userDetailsRepository.findById(userDetailsId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", userDetailsId);
                    return new EntityNotFoundException("Пользователь с ID " + userDetailsId + " не найден");
                });

        UserPhoto userPhoto = userPhotoRepository.findByUserDetails_Id(userDetailsId)
                .orElse(new UserPhoto());

        if (userPhoto.getId() != null) {
            log.debug("Обновление существующей фотографии для пользователя с ID: {}", userDetailsId);
        }

        userPhoto.setUserDetails(userDetails);
        userPhoto.setFileName(file.getOriginalFilename());
        userPhoto.setFileType(file.getContentType());
        userPhoto.setData(file.getBytes());
        userPhoto.setFileSize(file.getSize());

        log.info("fileName: {}", file.getOriginalFilename());
        log.info("contentType: {}", file.getContentType());
        log.info("fileSize: {}", file.getSize());
        log.info("bytes length: {}", file.getBytes().length);

        userPhotoRepository.save(userPhoto);
        log.info("Фотография для пользователя с ID {} успешно сохранена", userDetailsId);
    }

    /**
     * Удалить фотографию пользователя
     *
     * @param userDetailsId ID пользователя
     * @throws EntityNotFoundException если пользователь не найден
     */
    @Transactional
    public void deleteUserPhoto(Long userDetailsId) {
        log.info("Удаление фотографии пользователя с ID: {}", userDetailsId);
        if (!userDetailsRepository.existsById(userDetailsId)) {
            log.error("Пользователь с ID {} не найден", userDetailsId);
            throw new EntityNotFoundException("Пользователь с ID " + userDetailsId + " не найден");
        }

        userPhotoRepository.findByUserDetails_Id(userDetailsId)
                .ifPresentOrElse(userPhoto -> {
                    UserDetails userDetails = userPhoto.getUserDetails();
                    if (userDetails != null) {
                        userDetails.setUserPhoto(null);
                    }
                    userPhotoRepository.delete(userPhoto);
                    log.info("Фотография пользователя с ID {} успешно удалена", userDetailsId);
                }, () -> {
                    log.warn("Фотография для пользователя с ID {} не найдена, удаление не требуется", userDetailsId);
                });
    }
}