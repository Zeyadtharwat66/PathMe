package com.PathMe.course_reservation.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.PathMe.course_reservation.model.entity.CourseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.PathMe.course_reservation.model.DTO.CourseRequest;
import com.PathMe.course_reservation.model.DTO.CourseResponse;
import com.PathMe.course_reservation.model.DTO.CourseAdminPreviewResponse;
import com.PathMe.course_reservation.model.DTO.CourseUpdateRequest;
import com.PathMe.course_reservation.model.DTO.CourseUpdateResponse;
import com.PathMe.course_reservation.service.CoursesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.PathMe.course_reservation.model.DTO.CourseStatusRequest;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CoursesService coursesService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseResponse> create(
            @Valid @RequestPart("data") CourseRequest request,
            @RequestPart(value = "courseImg", required = false) MultipartFile courseImg,
            @RequestPart(value = "roadmapImg", required = false) MultipartFile roadmapImg) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(coursesService.createCourse(request, courseImg, roadmapImg));
    }
    @GetMapping("/top-enrolled")
    public List<CourseResponse> getTopEnrolledCourses(
            @RequestHeader(value = "lang", defaultValue = "EN") String language) {
        return coursesService.getTop3CoursesByEnrollments(language);
    }
    // Get All Courses with Pagination
    @GetMapping
    public Page<CourseResponse> getAllCourses(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestHeader(value = "lang", defaultValue = "EN") String language) {
        return coursesService.getAllCourses(page, size, language);
    }

    @GetMapping("/admin/preview")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<CourseAdminPreviewResponse> getAdminCoursePreviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size) {
        return coursesService.getAdminCoursePreviews(page, size);
    }

    @GetMapping("/admin/preview/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseAdminPreviewResponse> getAdminCoursePreview(@PathVariable UUID id) {
        return ResponseEntity.ok(coursesService.getAdminCoursePreview(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> updateCourseStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CourseStatusRequest request) {
        return ResponseEntity.ok(coursesService.updateCourseStatus(id, request.status()));
    }

    @PostMapping("/{id}/duplicate")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> duplicateCourse(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(coursesService.duplicateCourse(id));
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Page<CourseResponse> searchAdminCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) com.PathMe.course_reservation.model.enums.Status status,
            @RequestParam(required = false) List<String> levels,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate updatedTo,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestHeader(value = "lang", defaultValue = "EN") String language) {
        LocalDateTime createdFromDateTime = createdFrom != null ? createdFrom.atStartOfDay() : null;
        LocalDateTime createdToDateTime = createdTo != null ? createdTo.atTime(LocalTime.MAX) : null;
        LocalDateTime updatedFromDateTime = updatedFrom != null ? updatedFrom.atStartOfDay() : null;
        LocalDateTime updatedToDateTime = updatedTo != null ? updatedTo.atTime(LocalTime.MAX) : null;

        return coursesService.searchAdminCourses(
                search, status, levels, categories,
                createdFromDateTime, createdToDateTime, updatedFromDateTime, updatedToDateTime,
                sortBy, sort, page, size, language);
    }

    @PostMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<CourseResponse> restoreCourse(@PathVariable UUID id) {
        return ResponseEntity.ok(coursesService.restoreCourse(id));
    }

    // Get Course By ID
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> getCourseById(
            @PathVariable UUID id,
            @RequestHeader(value = "lang", defaultValue = "EN") String language) {
        CourseResponse response = coursesService.getCourseById(id, language);
        return ResponseEntity.ok(response);
    }

    // Update Course
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CourseUpdateResponse update(
            @PathVariable UUID id,
            @Valid @RequestPart("data") CourseUpdateRequest request,
            @RequestPart(value = "courseImg", required = false) MultipartFile courseImg,
            @RequestPart(value = "roadmapImg", required = false) MultipartFile roadmapImg) {
        return coursesService.updateCourse(id, request, courseImg, roadmapImg);
    }

    // Delete Course (Soft Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable UUID id) {
        coursesService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public Page<CourseResponse> findCoursesByFilters(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> levels,
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "lang", defaultValue = "EN") String language) {
        return coursesService.findCoursesByFilters(search, levels, categories, minPrice, maxPrice, page, size,
                language);
    }
}
