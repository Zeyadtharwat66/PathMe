package com.PathMe.course_reservation.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    public String storeImage(MultipartFile file);

    String copyImage(String imageUrl);

    void deleteImage(String imageUrl);

}
