package com.ff.commandes_service.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private String message;
    private int statusCode;
    private T data;
}
