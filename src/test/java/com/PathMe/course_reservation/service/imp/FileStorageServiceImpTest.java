package com.PathMe.course_reservation.service.imp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

class FileStorageServiceImpTest {

    @TempDir
    Path tempDir;

    @Test
    void storeImageAcceptsValidPngAndPersistsToDisk() throws IOException {
        FileStorageServiceImp service = new FileStorageServiceImp(tempDir.toString());
        byte[] bytes = createPngBytes();
        MockMultipartFile file = new MockMultipartFile("image", "avatar.png", "image/png", bytes);

        String savedPath = service.storeImage(file);

        assertNotNull(savedPath);
        assertEquals("/uploads/courses/", savedPath.substring(0, "/uploads/courses/".length()));
        assertEquals(1, Files.list(tempDir).count());
    }

    @Test
    void storeImageRejectsInvalidOrUnsupportedFile() {
        FileStorageServiceImp service = new FileStorageServiceImp(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("image", "not-image.txt", "text/plain", "garbage".getBytes());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.storeImage(file));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void copyImageReturnsNullForBlankPath() {
        FileStorageServiceImp service = new FileStorageServiceImp(tempDir.toString());

        assertNull(service.copyImage("  "));
    }

    private byte[] createPngBytes() throws IOException {
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }
}