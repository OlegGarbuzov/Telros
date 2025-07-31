package com.telros.telros.repository;

import com.telros.telros.model.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с фотографиями пользователей
 */
@Repository
public interface UserPhotoRepository extends JpaRepository<UserPhoto, Long> {

    /**
     * Поиск фотографии по идентификатору детальной информации пользователя
     *
     * @param userDetailsId идентификатор детальной информации пользователя
     * @return Optional с фотографией или пустой Optional
     */
    Optional<UserPhoto> findByUserDetails_Id(Long userDetailsId);
}