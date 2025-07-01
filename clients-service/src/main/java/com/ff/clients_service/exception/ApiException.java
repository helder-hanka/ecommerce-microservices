package com.ff.clients_service.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiException {
    private String message;
    private int status;
    private LocalDateTime timestamp;

}
