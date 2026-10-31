package com.PathMe.course_reservation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.PathMe.course_reservation.model.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.PathMe.course_reservation.model.entity.CourseEntity;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {
    Page<CourseEntity> findAll(Pageable pageable);

    Page<CourseEntity> findAll(Specification<CourseEntity> spec, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE courses SET deleted_at = NULL WHERE course_id = :id AND deleted_at IS NOT NULL", nativeQuery = true)
    int restoreSoftDeletedCourse(@Param("id") UUID id);

    @Modifying
    @Query("""
                DELETE FROM CourseEntity c
                WHERE c.deletedAt IS NOT NULL
                  AND c.deletedAt < :cutoff
                  AND NOT EXISTS (
                      SELECT e.id
                      FROM EnrollmentEntity e
                      WHERE e.course = c
                  )
            """)
    int permanentlyDeleteOldCourses(
            @Param("cutoff") LocalDateTime cutoff);
    long countByDeletedAtIsNullAndStatus(Status status);
    @Query("""
    SELECT c
    FROM CourseEntity c
    LEFT JOIN c.enrollments e
    WHERE c.deletedAt IS NULL
    GROUP BY c
    ORDER BY COUNT(e) DESC
    """)
    List<CourseEntity> findTop3CoursesByEnrollments(Pageable pageable);
}
