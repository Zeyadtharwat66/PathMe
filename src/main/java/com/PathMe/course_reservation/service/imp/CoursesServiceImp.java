package com.PathMe.course_reservation.service.imp;

import static com.PathMe.course_reservation.model.enums.Status.DRAFT;
import static com.PathMe.course_reservation.model.enums.Status.PUBLISH;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.PathMe.course_reservation.model.DTO.AboutUsStatsResponse;
import com.PathMe.course_reservation.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.PathMe.course_reservation.model.DTO.CourseRequest;
import com.PathMe.course_reservation.model.DTO.CourseResponse;
import com.PathMe.course_reservation.model.DTO.CourseAdminPreviewResponse;
import com.PathMe.course_reservation.model.DTO.CourseUpdateRequest;
import com.PathMe.course_reservation.model.DTO.CourseUpdateResponse;
import com.PathMe.course_reservation.model.entity.CategoryEntity;
import com.PathMe.course_reservation.model.entity.CourseEntity;
import com.PathMe.course_reservation.model.enums.Level;
import com.PathMe.course_reservation.repository.CategoryRepository;
import com.PathMe.course_reservation.repository.CourseRepository;
import com.PathMe.course_reservation.service.CoursesService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CoursesServiceImp implements CoursesService {
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageServiceImp fileStorageService;
    private final EnrollmentRepository enrollmentRepository;


    @Override
    @Transactional
    public CourseResponse createCourse(CourseRequest request, MultipartFile courseImg, MultipartFile roadmapImg) {
        CategoryEntity category = getCategory(request.categoryId());

        String courseImgUrl = null;
        String roadmapImgUrl = null;
        try {
            if (courseImg != null && !courseImg.isEmpty())
                courseImgUrl = fileStorageService.storeImage(courseImg);
            if (roadmapImg != null && !roadmapImg.isEmpty())
                roadmapImgUrl = fileStorageService.storeImage(roadmapImg);

            CourseEntity course = CourseEntity.builder()
                    .name(request.name())
                    .description(request.description())
                    .descriptionAr(request.descriptionAr())
                    .price(request.price())
                    .discount(request.discount())
                    .whatYouWillLearn(request.whatYouWillLearn())
                    .duration(request.duration())
                    .courseImg(courseImgUrl)
                    .roadmapImg(roadmapImgUrl)
                    .level(request.level())
                    .category(category)
                    .status(request.status() != null ? request.status() : DRAFT)
                    .build();

            return toResponse(courseRepository.save(course));
        } catch (RuntimeException ex) {
            // law 7aga fashalet, emsa7 ay sora et-save-et 3ashan matefdalsh orphan files
            fileStorageService.deleteImage(courseImgUrl);
            fileStorageService.deleteImage(roadmapImgUrl);
            throw ex;
        }
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<CourseResponse> getAllCourses(int page, int size) {
        return getAllCourses(page, size, "EN");
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<CourseResponse> getAllCourses(int page, int size, String language) {
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by("name").ascending());
        Specification<CourseEntity> published = (root, query, cb) -> cb.equal(
                root.get("status"), PUBLISH);
        return courseRepository.findAll(published, pageable)
                .map(course -> toResponse(course, language));
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<CourseAdminPreviewResponse> getAdminCoursePreviews(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("name").ascending());
        return courseRepository.findAll(pageable).map(this::toAdminPreviewResponse);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public CourseAdminPreviewResponse getAdminCoursePreview(UUID id) {
        return toAdminPreviewResponse(getCourse(id));
    }

    @Override
    public CourseResponse getCourseById(UUID id) {
        return getCourseById(id, "EN");
    }

    @Override
    public CourseResponse getCourseById(UUID id, String language) {
        CourseEntity course = courseRepository.findById(id)
                .filter(item -> item.getStatus() == PUBLISH)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        return toResponse(course, language);
    }

    @Override
    public CourseResponse updateCourseStatus(UUID id, com.PathMe.course_reservation.model.enums.Status status) {
        CourseEntity course = getCourse(id);
        course.setStatus(status);
        return toResponse(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseResponse duplicateCourse(UUID id) {
        CourseEntity original = getCourse(id);
        String courseImage = null;
        String roadmapImage = null;
        try {
            courseImage = fileStorageService.copyImage(original.getCourseImg());
            roadmapImage = fileStorageService.copyImage(original.getRoadmapImg());

            String copySuffix = " (Copy)";
            String copyName = original.getName();
            if (copyName.length() + copySuffix.length() > 200) {
                copyName = copyName.substring(0, 200 - copySuffix.length());
            }

            CourseEntity duplicate = CourseEntity.builder()
                    .name(copyName + copySuffix)
                    .description(original.getDescription())
                    .descriptionAr(original.getDescriptionAr())
                    .price(original.getPrice())
                    .discount(original.getDiscount())
                    .whatYouWillLearn(original.getWhatYouWillLearn() == null
                            ? null
                            : new ArrayList<>(original.getWhatYouWillLearn()))
                    .duration(original.getDuration())
                    .courseImg(courseImage)
                    .roadmapImg(roadmapImage)
                    .level(original.getLevel())
                    .category(original.getCategory())
                    .status(DRAFT)
                    .build();
            return toResponse(courseRepository.save(duplicate));
        } catch (RuntimeException ex) {
            fileStorageService.deleteImage(courseImage);
            fileStorageService.deleteImage(roadmapImage);
            throw ex;
        }
    }
    @Override
    public List<CourseResponse> getTop3CoursesByEnrollments() {
        return getTop3CoursesByEnrollments("EN");
    }

    @Override
    public List<CourseResponse> getTop3CoursesByEnrollments(String language) {
        return courseRepository.findTop3CoursesByEnrollments(
                        PageRequest.of(0, 3)
                ).stream()
                .map(course -> toResponse(course, language))
                .toList();
    }
    @Override
    @Transactional
    public CourseUpdateResponse updateCourse(UUID id, CourseUpdateRequest request, MultipartFile courseImg,
            MultipartFile roadmapImg) {
        CourseEntity course = getCourse(id);
        CategoryEntity category = getCategory(request.categoryId());
        String oldCourseImg = course.getCourseImg();
        String oldRoadmapImg = course.getRoadmapImg();
        String newCourseImg = null;
        String newRoadmapImg = null;
        try {
            if (courseImg != null && !courseImg.isEmpty())
                newCourseImg = fileStorageService.storeImage(courseImg);
            if (roadmapImg != null && !roadmapImg.isEmpty())
                newRoadmapImg = fileStorageService.storeImage(roadmapImg);
            if (courseImg == null)
                course.setCourseImg(null);
            if (roadmapImg == null)
                course.setRoadmapImg(null);
            course.setName(request.name());
            course.setDescription(request.description());
            course.setDescriptionAr(request.descriptionAr());
            course.setPrice(request.price());
            course.setDiscount(request.discount());
            course.setWhatYouWillLearn(request.whatYouWillLearn());
            course.setDuration(request.duration());
            course.setLevel(request.level());
            course.setCategory(category);
            course.setStatus(request.status());
            if (newCourseImg != null)
                course.setCourseImg(newCourseImg);
            if (newRoadmapImg != null)
                course.setRoadmapImg(newRoadmapImg);
            CourseEntity updated = courseRepository.save(course);
            // emsa7 el sowar el adeema bas ba3d ma el transaction ye-commit
            final String deleteCourseImg = newCourseImg != null ? oldCourseImg : null;
            final String deleteRoadmapImg = newRoadmapImg != null ? oldRoadmapImg : null;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    fileStorageService.deleteImage(deleteCourseImg);
                    fileStorageService.deleteImage(deleteRoadmapImg);
                }
            });
            return toUpdateResponse(updated);
        } catch (RuntimeException ex) {
            // fashal: emsa7 el sowar el gedeeda eli et-rafa3et
            fileStorageService.deleteImage(newCourseImg);
            fileStorageService.deleteImage(newRoadmapImg);
            throw ex;
        }
    }

    @Override
    public void deleteCourse(UUID id) {
        CourseEntity course = getCourse(id);
        course.setDeletedAt(LocalDateTime.now());
        courseRepository.save(course);
    }

    @Override
    @Transactional
    public CourseResponse restoreCourse(UUID id) {
        int restored = courseRepository.restoreSoftDeletedCourse(id);
        if (restored == 0) {
            throw new EntityNotFoundException("Soft-deleted course not found with id: " + id);
        }
        return toResponse(courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Restored course not found with id: " + id)));
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<CourseResponse> searchAdminCourses(
            String search,
            com.PathMe.course_reservation.model.enums.Status status,
            List<String> levels,
            List<String> categories,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            LocalDateTime updatedFrom,
            LocalDateTime updatedTo,
            String sortBy,
            Sort.Direction direction,
            int page,
            int size) {
        return searchAdminCourses(search, status, levels, categories, createdFrom, createdTo, updatedFrom, updatedTo,
                sortBy, direction, page, size, "EN");
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<CourseResponse> searchAdminCourses(
            String search,
            com.PathMe.course_reservation.model.enums.Status status,
            List<String> levels,
            List<String> categories,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            LocalDateTime updatedFrom,
            LocalDateTime updatedTo,
            String sortBy,
            Sort.Direction direction,
            int page,
            int size,
            String language) {
        if (page < 1 || size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Page must be >= 1 and size must be between 1 and 100");
        }
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "createdFrom must be before or equal to createdTo");
        }
        if (updatedFrom != null && updatedTo != null && updatedFrom.isAfter(updatedTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "updatedFrom must be before or equal to updatedTo");
        }

        String sortProperty = switch (sortBy == null ? "updatedAt" : sortBy) {
            case "createdAt" -> "createdAt";
            case "updatedAt" -> "updatedAt";
            default ->
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sortBy must be createdAt or updatedAt");
        };
        Sort.Direction sortDirection = direction == null ? Sort.Direction.DESC : direction;

        Specification<CourseEntity> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        if (search != null && !search.isBlank()) {
            String pattern = "%" + escapeLike(search.trim().toLowerCase()) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("description")), pattern, '\\'),
                    cb.like(cb.lower(root.get("descriptionAr")), pattern, '\\'),
                    cb.like(cb.lower(root.get("whatYouWillLearnText")), pattern, '\\')));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (levels != null && !levels.isEmpty()) {
            try {
                List<Level> levelEnums = levels.stream()
                        .filter(level -> level != null && !level.isBlank())
                        .map(level -> Level.valueOf(level.trim().toUpperCase()))
                        .toList();
                if (!levelEnums.isEmpty()) {
                    spec = spec.and((root, query, cb) -> root.get("level").in(levelEnums));
                }
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid level value");
            }
        }
        if (categories != null && !categories.isEmpty()) {
            List<String> normalizedCategories = categories.stream()
                    .filter(category -> category != null && !category.isBlank())
                    .map(String::trim)
                    .toList();
            if (!normalizedCategories.isEmpty()) {
                spec = spec.and((root, query, cb) -> root.get("category").get("name").in(normalizedCategories));
            }
        }
        if (createdFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
        }
        if (createdTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), createdTo));
        }
        if (updatedFrom != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("updatedAt"), updatedFrom));
        }
        if (updatedTo != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("updatedAt"), updatedTo));
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(sortDirection, sortProperty));
        return courseRepository.findAll(spec, pageable).map(course -> toResponse(course, language));
    }

    private CourseEntity getCourse(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with id: " + id));
    }

    private CategoryEntity getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category not found with id: " + id));
    }

    private CourseResponse toResponse(CourseEntity course) {
        return toResponse(course, "EN");
    }

    private CourseResponse toResponse(CourseEntity course, String language) {
        String description = language != null && language.toLowerCase(java.util.Locale.ROOT).startsWith("ar")
                && course.getDescriptionAr() != null && !course.getDescriptionAr().isBlank()
                        ? course.getDescriptionAr()
                        : course.getDescription();
        return new CourseResponse(
                course.getId(),
                course.getName(),
                description,
                course.getPrice(),
                course.getDiscount(),
                course.getDiscount() == null ? null
                        : course.getPrice() - (course.getPrice() * course.getDiscount() / 100.0),
                course.getWhatYouWillLearn(),
                course.getDuration(),
                toUrl(course.getCourseImg()),
                course.getLevel(),
                course.getCategory().getName(),
                toUrl(course.getRoadmapImg()),
                course.getStatus());
    }

    private CourseUpdateResponse toUpdateResponse(CourseEntity course) {
        return new CourseUpdateResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getDescriptionAr(),
                course.getPrice(),
                course.getDiscount(),
                course.getDiscount() == null ? null
                        : course.getPrice() - (course.getPrice() * course.getDiscount() / 100.0),
                course.getWhatYouWillLearn(),
                course.getDuration(),
                toUrl(course.getCourseImg()),
                course.getLevel(),
                course.getCategory().getName(),
                toUrl(course.getRoadmapImg()),
                course.getStatus());
    }

    private CourseAdminPreviewResponse toAdminPreviewResponse(CourseEntity course) {
        return new CourseAdminPreviewResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getDescriptionAr(),
                course.getPrice(),
                course.getDiscount(),
                course.getDiscount() == null ? null
                        : course.getPrice() - (course.getPrice() * course.getDiscount() / 100.0),
                course.getWhatYouWillLearn(),
                course.getDuration(),
                toUrl(course.getCourseImg()),
                course.getLevel(),
                course.getCategory().getName(),
                toUrl(course.getRoadmapImg()),
                course.getStatus());
    }

    public Page<CourseResponse> findCoursesByFilters(String search, List<String> levels, List<String> categories,
            Double minPrice, Double maxPrice, int page, int size) {
        return findCoursesByFilters(search, levels, categories, minPrice, maxPrice, page, size, "EN");
    }

    @Override
    public Page<CourseResponse> findCoursesByFilters(String search, List<String> levels, List<String> categories,
            Double minPrice, Double maxPrice, int page, int size, String language) {
        Specification<CourseEntity> spec = (root, query, cb) -> cb.conjunction();
        spec = spec.and((root, query, cb) -> cb.equal(
                root.get("status"), PUBLISH));

        // Text search (name + description)
        if (search != null && !search.isBlank()) {
            String pattern = "%" + escapeLike(search.trim().toLowerCase()) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("description")), pattern, '\\'),
                    cb.like(cb.lower(root.get("descriptionAr")), pattern, '\\'),
                    cb.like(cb.lower(root.get("whatYouWillLearnText")), pattern, '\\')));
        }

        // Filter by levels
        if (levels != null && !levels.isEmpty()) {
            List<Level> levelEnums;

            try {
                levelEnums = levels.stream()
                        .map(level -> Level.valueOf(
                                level.trim().toUpperCase()))
                        .toList();

            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid level value");
            }

            spec = spec.and((root, query, cb) -> root.get("level").in(levelEnums));
        }

        // Filter by categories
        if (categories != null && !categories.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("category").get("name").in(categories));
        }

        // Filter by minimum price
        if (minPrice != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(
                    root.get("price"),
                    minPrice));
        }

        // Filter by maximum price
        if (maxPrice != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(
                    root.get("price"),
                    maxPrice));
        }
        // Pagination
        Pageable pageable = PageRequest.of(page - 1, size);
        return courseRepository.findAll(spec, pageable).map(course -> toResponse(course, language));
    }

    @Value("${app.base-url}")
    private String baseUrl;

    private String toUrl(String path) {
        if (path == null || path.startsWith("http"))
            return path; // يسيب لينكات placehold.co زي ما هي
        return baseUrl + path;
    }

    private String escapeLike(String s) {
        return s.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    public AboutUsStatsResponse aboutUs(){
        long courseCount = courseRepository.countByDeletedAtIsNullAndStatus(PUBLISH);

        long studentsEnrolled = 228+enrollmentRepository.count();

        long learningCategoriesCount =
                categoryRepository.countByDeletedAtIsNull();

        return new AboutUsStatsResponse(
                courseCount,
                studentsEnrolled,
                learningCategoriesCount
        );
    }
}