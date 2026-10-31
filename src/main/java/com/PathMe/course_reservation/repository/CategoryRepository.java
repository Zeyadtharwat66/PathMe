package com.PathMe.course_reservation.repository;

import com.PathMe.course_reservation.model.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    boolean existsByNameIgnoreCase(String name);
    long countByDeletedAtIsNull();

    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
