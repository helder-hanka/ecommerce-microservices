package com.ff.products_service.utils;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public class ResponseBuilder {
    public static <T> ApiRes<T> success(String message, T data) {
        return new ApiRes<>(
                message,
                HttpStatus.OK.value(),
                LocalDateTime.now(),
                data
        );
    }

    public static <T> ApiRes<T> created(String message, T data) {
        return new ApiRes<>(
                message,
                HttpStatus.CREATED.value(),
                LocalDateTime.now(),
                data
        );
    }

    public static <T> ApiRes<T> error(String message, int statusCode) {
        return new ApiRes<>(
                message,
                statusCode,
                LocalDateTime.now(),
                null
        );
    }
}
