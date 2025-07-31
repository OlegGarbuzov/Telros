package com.telros.telros.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для сообщения об ошибке
 */
@Data
@AllArgsConstructor
public class MessageResponse {

    private String message;
}