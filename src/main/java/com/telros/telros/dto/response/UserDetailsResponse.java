package com.telros.telros.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для ответа с детальной информацией о пользователе
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsResponse {

    private Long id;
    private String lastName; // Фамилия
    private String firstName; // Имя
    private String middleName; // Отчество
    private LocalDate birthDate; // Дата рождения
    private String email; // Электронная почта
    private String phoneNumber; // Номер телефона
    private boolean hasPhoto; // Наличие фотографии
    private String photoUrl; // URL фотографии
}