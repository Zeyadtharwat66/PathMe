package com.PathMe.course_reservation.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.PathMe.course_reservation.model.DTO.AboutUsStatsResponse;
import com.PathMe.course_reservation.model.DTO.CourseAdminPreviewResponse;
import com.PathMe.course_reservation.model.entity.CourseEntity;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.PathMe.course_reservation.model.DTO.CourseRequest;
import com.PathMe.course_reservation.model.DTO.CourseResponse;
import com.PathMe.course_reservation.model.DTO.CourseUpdateRequest;
import com.PathMe.course_reservation.model.DTO.CourseUpdateResponse;

public interface CoursesService {
    CourseResponse createCourse(CourseRequest request,
            MultipartFile courseImg,
            MultipartFile roadmapImg);

    Page<CourseResponse> getAllCourses(int page, int size);
    Page<CourseResponse> getAllCourses(int page, int size, String language);

    Page<CourseAdminPreviewResponse> getAdminCoursePreviews(int page, int size);

    CourseAdminPreviewResponse getAdminCoursePreview(UUID id);

    CourseResponse getCourseById(UUID id);
    CourseResponse getCourseById(UUID id, String language);

    CourseResponse updateCourseStatus(UUID id, com.PathMe.course_reservation.model.enums.Status status);

    CourseResponse duplicateCourse(UUID id);

    CourseUpdateResponse updateCourse(UUID id,
            CourseUpdateRequest request,
            MultipartFile courseImg,
            MultipartFile roadmapImg);

    void deleteCourse(UUID id);

    CourseResponse restoreCourse(UUID id);

    Page<CourseResponse> searchAdminCourses(
            String search,
            com.PathMe.course_reservation.model.enums.Status status,
            List<String> levels,
            List<String> categories,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            LocalDateTime updatedFrom,
            LocalDateTime updatedTo,
            String sortBy,
            org.springframework.data.domain.Sort.Direction direction,
            int page,
            int size);
    Page<CourseResponse> searchAdminCourses(
            String search,
            com.PathMe.course_reservation.model.enums.Status status,
            List<String> levels,
            List<String> categories,
            LocalDateTime createdFrom,
            LocalDateTime createdTo,
            LocalDateTime updatedFrom,
            LocalDateTime updatedTo,
            String sortBy,
            org.springframework.data.domain.Sort.Direction direction,
            int page,
            int size,
            String language);

    public Page<CourseResponse> findCoursesByFilters(String search, List<String> levels,
            List<String> categories,
            Double minPrice,
            Double maxPrice,
            int page,
            int size);
    public Page<CourseResponse> findCoursesByFilters(String search, List<String> levels,
            List<String> categories,
            Double minPrice,
            Double maxPrice,
            int page,
            int size,
            String language);
    public AboutUsStatsResponse aboutUs();
    public List<CourseResponse> getTop3CoursesByEnrollments();
    public List<CourseResponse> getTop3CoursesByEnrollments(String language);
    }
