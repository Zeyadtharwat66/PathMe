package com.PathMe.course_reservation.controller;

import com.PathMe.course_reservation.model.DTO.AboutUsStatsResponse;
import com.PathMe.course_reservation.service.CoursesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/about-us")
@RequiredArgsConstructor
public class AboutUsController {

    private final CoursesService aboutUsService;

    @GetMapping("/stats")
    public ResponseEntity<AboutUsStatsResponse> getStats() {
        return ResponseEntity.ok(aboutUsService.aboutUs());
    }
}