package com.telros.telros.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO для запроса на создание/обновление детальной информации о пользователе
 */
@Data
public class UserDetailsRequest {

    @NotBlank
    @Size(max = 50)
    private String lastName; // Фамилия

    @NotBlank
    @Size(max = 50)
    private String firstName; // Имя

    @Size(max = 50)
    private String middleName; // Отчество

    private LocalDate birthDate; // Дата рождения

    @Size(max = 20)
    private String phoneNumber; // Номер телефона
}