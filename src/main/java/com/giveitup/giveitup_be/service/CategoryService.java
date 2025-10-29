package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.request.CategoryRequest;
import com.giveitup.giveitup_be.dto.request.SearchListCategoryRequest;
import com.giveitup.giveitup_be.dto.response.CategoryResponse;
import com.giveitup.giveitup_be.entity.CategoryEntity;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.CategoryMapper;
import com.giveitup.giveitup_be.repository.CategoryRepository;
import com.giveitup.giveitup_be.specification.CategorySpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper  categoryMapper;
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse createCategory(CategoryRequest request) {
        CategoryEntity categoryEntity = categoryMapper.toCategory(request);
        try {
            categoryEntity = categoryRepository.save(categoryEntity);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.CATEGORY_EXISTED);
        }
        return categoryMapper.toCategoryResponse(categoryEntity);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest request) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        categoryMapper.updateCategory(categoryEntity, request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(categoryEntity));
    }
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCategory(Long categoryId) {
        CategoryEntity categoryEntity = categoryRepository.findById(categoryId).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        categoryRepository.deleteById(categoryEntity.getId());
    }
    public Page<CategoryResponse> getCategories(SearchListCategoryRequest request) {
        Specification<CategoryEntity> spec =
                Specification.allOf(CategorySpecification.hasName(request.getCategoryName()));

        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
        Pageable pageable = PageRequest.of(
                pageIndex,
                request.getPageSize(),
                Sort.by("categoryName").ascending() // ✅ Sửa lại đúng trường
        );
        Page<CategoryEntity> page = categoryRepository.findAll(spec, pageable);
        log.info("Found {} categories", page.getTotalElements());
        return page.map(categoryMapper::toCategoryResponse); // ✅ Sửa lại đúng hàm mapper
    }
    public CategoryResponse getCategoryById(Long categoryId) {
        return categoryMapper.toCategoryResponse(categoryRepository.findById(categoryId).orElseThrow(() ->new AppException(ErrorCode.CATEGORY_NOT_EXISTED)));
    }

}
