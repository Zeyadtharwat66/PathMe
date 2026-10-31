package com.PathMe.course_reservation.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.PathMe.course_reservation.model.entity.EnrollmentEntity;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, UUID> {
        boolean existsByUser_IdAndCourse_Id(UUID userId, UUID courseId);

        Page<EnrollmentEntity> findByUser_Id(UUID userId, Pageable pageable);
        long count();

        List<EnrollmentEntity> findAllByUser_Id(UUID userId);
        List<EnrollmentEntity> findTop3ByOrderByCourse_IdDesc();
        List<EnrollmentEntity> findAllByCourse_Id(UUID courseId);
}
