package com.telros.telros.repository;

import com.telros.telros.model.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с детальной информацией о пользователе
 */
@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {

    /**
     * Поиск детальной информации по идентификатору пользователя
     *
     * @param userId идентификатор пользователя
     * @return Optional с детальной информацией или пустой Optional
     */
    Optional<UserDetails> findByUserId(Long userId);
}