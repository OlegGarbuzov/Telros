package com.telros.telros.repository;

import com.telros.telros.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с пользователями
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Поиск пользователя по имени пользователя
     *
     * @param username имя пользователя
     * @return Optional с пользователем или пустой Optional
     */
    Optional<User> findByUsername(String username);

    /**
     * Проверка существования пользователя с указанным именем
     *
     * @param username имя пользователя
     * @return true если пользователь существует, иначе false
     */
    Boolean existsByUsername(String username);

    /**
     * Проверка существования пользователя с указанным email
     *
     * @param email email пользователя
     * @return true если пользователь существует, иначе false
     */
    Boolean existsByEmail(String email);
}