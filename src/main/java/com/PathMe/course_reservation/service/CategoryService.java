package com.PathMe.course_reservation.service;

import com.PathMe.course_reservation.model.DTO.CategoryRequest;
import com.PathMe.course_reservation.model.DTO.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(UUID id);

    CategoryResponse updateCategory(UUID id, CategoryRequest request);

    void deleteCategory(UUID id);
}