package com.PathMe.course_reservation.service.imp;

import static com.PathMe.course_reservation.model.enums.Status.DRAFT;
import static com.PathMe.course_reservation.model.enums.Status.PUBLISH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.PathMe.course_reservation.model.enums.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import com.PathMe.course_reservation.model.DTO.CourseResponse;
import com.PathMe.course_reservation.model.DTO.CourseRequest;
import com.PathMe.course_reservation.model.DTO.CourseAdminPreviewResponse;
import com.PathMe.course_reservation.model.DTO.CourseUpdateRequest;
import com.PathMe.course_reservation.model.DTO.CourseUpdateResponse;
import com.PathMe.course_reservation.model.entity.CategoryEntity;
import com.PathMe.course_reservation.model.entity.CourseEntity;
import com.PathMe.course_reservation.model.enums.Level;
import com.PathMe.course_reservation.repository.CategoryRepository;
import com.PathMe.course_reservation.repository.CourseRepository;
import com.PathMe.course_reservation.repository.EnrollmentRepository;
import com.PathMe.course_reservation.service.imp.CoursesServiceImp;
import com.PathMe.course_reservation.service.imp.FileStorageServiceImp;

@ExtendWith(MockitoExtension.class)
class CoursesServiceImpTest {
    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FileStorageServiceImp fileStorageService;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private CoursesServiceImp coursesService;

    @Test
    void publicListingUsesPublishedCourseFilter() {
        CourseEntity published = course("Published", PUBLISH);
        when(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(published)));

        var result = coursesService.getAllCourses(1, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(PUBLISH, result.getContent().get(0).status());
        verify(courseRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void adminSearchRejectsInvalidDateRange() {
        LocalDateTime createdFrom = LocalDateTime.of(2026, 2, 10, 0, 0);
        LocalDateTime createdTo = LocalDateTime.of(2026, 2, 5, 0, 0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> coursesService.searchAdminCourses(
                        null,
                        null,
                        null,
                        null,
                        createdFrom,
                        createdTo,
                        null,
                        null,
                        "createdAt",
                        Sort.Direction.ASC,
                        1,
                        10));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void findCoursesByFiltersUsesLevelsCategoriesAndPriceRange() {
        CourseEntity course = course("Java Basics", PUBLISH);
        when(courseRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(course)));

        var result = coursesService.findCoursesByFilters(
                "java",
                List.of("BEGINNER"),
                List.of("Engineering"),
                50.0,
                200.0,
                1,
                10);

        assertEquals(1, result.getTotalElements());
        verify(courseRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void responseCalculatesDiscountedPriceWithoutChangingBasePrice() {
        CourseEntity discounted = course("Discounted", PUBLISH);
        discounted.setPrice(120.0);
        discounted.setDiscount(25.0);
        when(courseRepository.findById(discounted.getId())).thenReturn(Optional.of(discounted));

        CourseAdminPreviewResponse result = coursesService.getAdminCoursePreview(discounted.getId());

        assertEquals(120.0, discounted.getPrice());
        assertEquals(120.0, result.price());
        assertEquals(25.0, result.discount());
        assertEquals(90.0, result.discountedPrice());
    }

    @Test
    void adminCoursePreviewIncludesBothDescriptions() {
        CourseEntity course = course("Java", PUBLISH);
        course.setDescriptionAr("وصف الدورة");
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CourseAdminPreviewResponse result = coursesService.getAdminCoursePreview(course.getId());

        assertEquals("Course description", result.description());
        assertEquals("وصف الدورة", result.descriptionAr());
    }

    @Test
    void adminCoursePreviewListIncludesBothDescriptions() {
        CourseEntity course = course("Java", PUBLISH);
        course.setDescriptionAr("وصف الدورة");
        when(courseRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(course)));

        var result = coursesService.getAdminCoursePreviews(1, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Course description", result.getContent().get(0).description());
        assertEquals("وصف الدورة", result.getContent().get(0).descriptionAr());
    }

    @Test
    void courseResponseUsesArabicDescriptionForArabicLanguage() {
        CourseEntity course = course("Java", PUBLISH);
        course.setDescriptionAr("وصف الدورة");
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CourseResponse result = coursesService.getCourseById(course.getId(), "ar-EG");

        assertEquals("وصف الدورة", result.description());
    }

    @Test
    void courseResponseFallsBackToEnglishWhenArabicDescriptionIsMissing() {
        CourseEntity course = course("Java", PUBLISH);
        when(courseRepository.findById(course.getId())).thenReturn(Optional.of(course));

        CourseResponse result = coursesService.getCourseById(course.getId(), "ar");

        assertEquals("Course description", result.description());
    }

    @Test
    void createCourseStoresBothDescriptions() {
        UUID categoryId = UUID.randomUUID();
        CategoryEntity category = new CategoryEntity();
        category.setName("Engineering");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(courseRepository.save(any(CourseEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CourseRequest request = new CourseRequest(
                "Java",
                "Course description",
                "وصف الدورة",
                100.0,
                null,
                List.of("Java"),
                "8 weeks",
                Level.BEGINNER,
                categoryId,
                PUBLISH);

        coursesService.createCourse(request, null, null);

        ArgumentCaptor<CourseEntity> courseCaptor = ArgumentCaptor.forClass(CourseEntity.class);
        verify(courseRepository).save(courseCaptor.capture());
        assertEquals("Course description", courseCaptor.getValue().getDescription());
        assertEquals("وصف الدورة", courseCaptor.getValue().getDescriptionAr());
        assertEquals(PUBLISH, courseCaptor.getValue().getStatus());
    }

    @Test
    void createCourseDefaultsToDraftWhenStatusIsMissing() {
        UUID categoryId = UUID.randomUUID();
        CategoryEntity category = new CategoryEntity();
        category.setName("Engineering");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(courseRepository.save(any(CourseEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CourseRequest request = new CourseRequest(
                "Java",
                "Course description",
                "وصف الدورة",
                100.0,
                null,
                List.of("Java"),
                "8 weeks",
                Level.BEGINNER,
                categoryId,
                null);

        coursesService.createCourse(request, null, null);

        ArgumentCaptor<CourseEntity> courseCaptor = ArgumentCaptor.forClass(CourseEntity.class);
        verify(courseRepository).save(courseCaptor.capture());
        assertEquals(DRAFT, courseCaptor.getValue().getStatus());
    }

    @Test
    void updateCourseStoresBothEditedDescriptions() {
        UUID courseId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        CategoryEntity category = new CategoryEntity();
        category.setName("Engineering");
        CourseEntity course = course("Java", PUBLISH);
        course.setId(courseId);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(courseRepository.save(course)).thenReturn(course);
        CourseUpdateRequest request = new CourseUpdateRequest(
                "Java",
                "Updated course description",
                "وصف الدورة المحدث",
                100.0,
                null,
                List.of("Java"),
                "8 weeks",
                Level.BEGINNER,
                categoryId,
                PUBLISH);

        TransactionSynchronizationManager.initSynchronization();
        try {
            CourseUpdateResponse response = coursesService.updateCourse(courseId, request, null, null);

            assertEquals("Updated course description", course.getDescription());
            assertEquals("وصف الدورة المحدث", course.getDescriptionAr());
            assertEquals("Updated course description", response.description());
            assertEquals("وصف الدورة المحدث", response.descriptionAr());
            verify(courseRepository).save(course);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void duplicateCourseCreatesIndependentDraftWithoutEnrollments() {
        CategoryEntity category = new CategoryEntity();
        category.setName("Engineering");
        CourseEntity original = course("Java", PUBLISH);
        original.setCategory(category);
        original.setCourseImg("/uploads/courses/original.jpg");
        original.setRoadmapImg("https://cdn.example.test/roadmap.png");
        original.setDescriptionAr("وصف الدورة");
        original.setWhatYouWillLearn(List.of("Spring", "JPA"));
        when(courseRepository.findById(original.getId())).thenReturn(Optional.of(original));
        when(fileStorageService.copyImage("/uploads/courses/original.jpg")).thenReturn("/uploads/courses/copy.jpg");
        when(fileStorageService.copyImage("https://cdn.example.test/roadmap.png"))
                .thenReturn("https://cdn.example.test/roadmap.png");
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse result = coursesService.duplicateCourse(original.getId());

        ArgumentCaptor<CourseEntity> duplicateCaptor = ArgumentCaptor.forClass(CourseEntity.class);
        verify(courseRepository).save(duplicateCaptor.capture());
        CourseEntity duplicate = duplicateCaptor.getValue();
        assertEquals("Java (Copy)", duplicate.getName());
        assertEquals(DRAFT, duplicate.getStatus());
        assertEquals("/uploads/courses/copy.jpg", duplicate.getCourseImg());
        assertEquals(category, duplicate.getCategory());
        assertEquals(List.of("Spring", "JPA"), duplicate.getWhatYouWillLearn());
        assertEquals("وصف الدورة", duplicate.getDescriptionAr());
        assertNotEquals(original.getId(), duplicate.getId());
        assertEquals(DRAFT, result.status());
        verify(fileStorageService).copyImage("/uploads/courses/original.jpg");
        verifyNoInteractions(categoryRepository);
    }

    private CourseEntity course(String name, Status status) {
        CategoryEntity category = new CategoryEntity();
        category.setName("Engineering");
        CourseEntity course = CourseEntity.builder()
                .id(UUID.randomUUID())
                .name(name)
                .description("Course description")
                .price(100.0)
                .whatYouWillLearn(List.of("Java"))
                .duration("8 weeks")
                .level(Level.BEGINNER)
                .category(category)
                .status(status)
                .build();
        return course;
    }
}
