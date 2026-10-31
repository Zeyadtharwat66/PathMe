package com.PathMe.course_reservation.repository;

import com.PathMe.course_reservation.model.entity.EnrollmentEntity;
import com.PathMe.course_reservation.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserRepo extends JpaRepository<UserEntity, UUID> {
    public Optional<UserEntity> findByEmail(String email);
    public Boolean existsByUsername(String username);
    public Optional<UserEntity> findByUsername(String username);
    public Boolean existsByEmail(String email);
    Optional<UserEntity> findByEmailIgnoreCase(String email);
    Optional<UserEntity> findByGoogleSub(String googleSub);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);
    Optional<UserEntity> findByUsernameIgnoreCase(String username);
    @Query(value = """
    SELECT phone_number
    FROM users
    WHERE user_id = '4a43a778-dee7-49e8-ac41-953c56f9c8c8'
    """, nativeQuery = true)
    String findPhoneNumberByUserId();

}
