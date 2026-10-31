package com.PathMe.course_reservation.config;

import com.PathMe.course_reservation.repository.CourseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SoftDeleteCleanupJob {

    private final CourseRepository courseRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupDeletedCourses() {

        LocalDateTime cutoff =
                LocalDateTime.now().minusDays(30);

        int deletedCount =
                courseRepository.permanentlyDeleteOldCourses(cutoff);

        log.info(
                "Soft delete cleanup completed. Deleted courses: {}",
                deletedCount
        );
    }
}