package com.PathMe.course_reservation.service.imp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.PathMe.course_reservation.model.DTO.CategoryRequest;
import com.PathMe.course_reservation.model.DTO.CategoryResponse;
import com.PathMe.course_reservation.model.entity.CategoryEntity;
import com.PathMe.course_reservation.repository.CategoryRepository;
import com.PathMe.course_reservation.service.imp.CategoryServiceImpl;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void createCategorySavesNewCategory() {
        CategoryRequest request = new CategoryRequest("Design");
        when(categoryRepository.existsByNameIgnoreCase("Design")).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> {
            CategoryEntity category = invocation.getArgument(0);
            category.setId(UUID.randomUUID());
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());
            return category;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertEquals("Design", response.name());
        assertNotNull(response.id());
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    void createCategoryRejectsDuplicateName() {
        when(categoryRepository.existsByNameIgnoreCase("Design")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.createCategory(new CategoryRequest("Design")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void getAllCategoriesReturnsAllMappedCategories() {
        UUID id = UUID.randomUUID();
        CategoryEntity category = CategoryEntity.builder()
                .id(id)
                .name("Design")
                .createdAt(LocalDateTime.of(2025, 1, 1, 8, 0))
                .updatedAt(LocalDateTime.of(2025, 1, 2, 8, 0))
                .build();
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponse> categories = categoryService.getAllCategories();

        assertEquals(1, categories.size());
        assertEquals("Design", categories.get(0).name());
    }

    @Test
    void updateCategoryRejectsDuplicateNameForAnotherCategory() {
        UUID id = UUID.randomUUID();
        CategoryEntity category = CategoryEntity.builder().id(id).name("Old").build();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("New", id)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.updateCategory(id, new CategoryRequest("New")));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void deleteCategoryMarksEntityAsDeleted() {
        UUID id = UUID.randomUUID();
        CategoryEntity category = CategoryEntity.builder().id(id).name("Design").build();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        categoryService.deleteCategory(id);

        assertNotNull(category.getDeletedAt());
        verify(categoryRepository).save(category);
    }
}