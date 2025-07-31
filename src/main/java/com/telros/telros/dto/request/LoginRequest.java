package com.telros.telros.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса на вход в систему
 */
@Data
public class LoginRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}