package com.PathMe.course_reservation.service.imp;

import com.PathMe.course_reservation.model.DTO.CategoryRequest;
import com.PathMe.course_reservation.model.DTO.CategoryResponse;
import com.PathMe.course_reservation.model.entity.CategoryEntity;
import com.PathMe.course_reservation.repository.CategoryRepository;
import com.PathMe.course_reservation.service.CategoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {

        if (categoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category name already exists"
            );
        }

        CategoryEntity category = CategoryEntity.builder()
                .name(request.name().trim())
                .build();

        CategoryEntity savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {

        CategoryEntity category = findCategoryOrThrow(id);

        return toResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(
            UUID id,
            CategoryRequest request
    ) {

        CategoryEntity category = findCategoryOrThrow(id);

        String newName = request.name().trim();

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(
                newName, id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Category name already exists"
            );
        }

        category.setName(newName);

        CategoryEntity updatedCategory =
                categoryRepository.save(category);

        return toResponse(updatedCategory);
    }

    @Override
    public void deleteCategory(UUID id) {

        CategoryEntity category = findCategoryOrThrow(id);

        category.setDeletedAt(LocalDateTime.now());

        categoryRepository.save(category);
    }

    private CategoryEntity findCategoryOrThrow(UUID id) {

        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                ));
    }

    private CategoryResponse toResponse(CategoryEntity category) {

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}