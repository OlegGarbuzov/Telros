package com.telros.telros.repository;

import com.telros.telros.model.ERole;
import com.telros.telros.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с ролями пользователей
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * Поиск роли по имени
     *
     * @param name имя роли
     * @return Optional с ролью или пустой Optional
     */
    Optional<Role> findByName(ERole name);
}