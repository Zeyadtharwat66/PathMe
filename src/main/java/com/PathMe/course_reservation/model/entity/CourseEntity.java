package com.PathMe.course_reservation.model.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.PathMe.course_reservation.model.enums.Status;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

import com.PathMe.course_reservation.model.enums.Level;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Where(clause = "deleted_at is null")
public class CourseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "course_id")
    private UUID id;
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;
    private Double price;

    @Column(nullable = true)
    private Double discount;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "what_you_will_learn", columnDefinition = "jsonb")
    private List<String> whatYouWillLearn;
    private String duration;
    @Column(name = "course_img")
    @Nullable
    private String courseImg;
    @Column(name = "roadmap_img")
    @Nullable
    private String roadmapImg;
    private Level level;
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    @Enumerated(EnumType.STRING)
    Status status;
    @JsonManagedReference
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<EnrollmentEntity> enrollments;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;
    @Formula("cast(what_you_will_learn as varchar)")
    private String whatYouWillLearnText;
}
