package com.ff.products_service.dto;

import lombok.Data;

@Data
public abstract class AbstractImageRequest {
    private Boolean main;
    private Boolean toDelete;
}