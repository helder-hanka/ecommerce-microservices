package com.ff.products_service.utils;

import com.ff.products_service.dto.ImageResponseDTO;
import com.ff.products_service.dto.ProductResponseDTO;
import com.ff.products_service.entity.Image;
import com.ff.products_service.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
    //@Mapping(source = "adminId", target = "adminId")
    ProductResponseDTO toProductResponseDTO(Product product);
    ImageResponseDTO toImageResponseDTO(Image image);
}