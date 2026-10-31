package com.PathMe.course_reservation.controller;

import com.PathMe.course_reservation.model.DTO.FileUploadResponse;
import com.PathMe.course_reservation.service.FileStorageService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(
            value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<FileUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file) {

        String path = fileStorageService.storeImage(file);

        return ResponseEntity.ok(new FileUploadResponse(path));
    }
}