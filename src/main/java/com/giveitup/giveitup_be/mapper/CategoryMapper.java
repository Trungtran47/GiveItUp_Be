package com.giveitup.giveitup_be.mapper;


import com.giveitup.giveitup_be.dto.request.CategoryRequest;
import com.giveitup.giveitup_be.dto.request.UserUpdateRequest;
import com.giveitup.giveitup_be.dto.response.CategoryResponse;
import com.giveitup.giveitup_be.entity.CategoryEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryEntity toCategory(CategoryRequest request);

    CategoryResponse toCategoryResponse(CategoryEntity categoryEntity);

//    @Mapping(target = "role", ignore = true)
    void updateCategory(@MappingTarget CategoryEntity categoryEntity, CategoryRequest request);
}
