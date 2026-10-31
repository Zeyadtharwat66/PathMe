package com.PathMe.course_reservation.service.imp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.PathMe.course_reservation.service.FileStorageService;

@Service
public class FileStorageServiceImp implements FileStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private static final Set<String> ALLOWED_FORMATS = Set.of("jpeg", "jpg", "png", "gif");

    private final Path storageLocation;

    public FileStorageServiceImp(
            @Value("${app.file.storage-location}") String location) {
        this.storageLocation = Paths.get(location)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(storageLocation);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Could not initialize image storage", ex);
        }
    }

    public String storeImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select an image");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Image size must not exceed 5 MB");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null
                        ? ""
                        : file.getOriginalFilename());

        if (originalFilename.isBlank()
                || originalFilename.contains("..")
                || originalFilename.contains("/")
                || originalFilename.contains("\\")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid filename");
        }

        String format = detectImageFormat(file);

        if (!ALLOWED_FORMATS.contains(format)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only JPEG, PNG, and GIF images are allowed");
        }

        String extension = format.equals("jpeg")
                ? "jpg"
                : format;

        String filename = UUID.randomUUID() + "." + extension;
        Path target = storageLocation.resolve(filename).normalize();

        if (!target.startsWith(storageLocation)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file path");
        }

        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target);
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save image");
        }

        return "/uploads/courses/" + filename;
    }

    @Override
    public String copyImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        if (!imageUrl.startsWith("/uploads/courses/")) {
            return imageUrl;
        }

        String filename = imageUrl.substring("/uploads/courses/".length());
        if (filename.isBlank() || filename.contains("/") || filename.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image path");
        }

        Path source = storageLocation.resolve(filename).normalize();
        String extension = StringUtils.getFilenameExtension(filename);
        String copyName = UUID.randomUUID() + (extension == null ? "" : "." + extension);
        Path target = storageLocation.resolve(copyName).normalize();
        if (!source.startsWith(storageLocation) || !target.startsWith(storageLocation)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image path");
        }

        try {
            Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
            return "/uploads/courses/" + copyName;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not duplicate course image", ex);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        // تجاهل أي لينك خارجي أو قيمة فاضية، امسح بس الملفات المحلية
        if (imageUrl == null || !imageUrl.startsWith("/uploads/courses/")) {
            return;
        }

        try {
            String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path file = storageLocation.resolve(filename).normalize();

            if (file.startsWith(storageLocation)) { // حماية من path traversal
                Files.deleteIfExists(file);
            }
        } catch (IOException | RuntimeException ex) {
            // محاولة مسح فاشلة ماينفعش تبوّظ الـ response
        }
    }

    private String detectImageFormat(MultipartFile file) {

        try (InputStream input = file.getInputStream();
                ImageInputStream imageInput = ImageIO.createImageInputStream(input)) {

            if (imageInput == null) {
                throw new IOException("Invalid image");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);

            if (!readers.hasNext()) {
                throw new IOException("Unsupported image");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInput, true, true);

                String format = reader.getFormatName()
                        .toLowerCase(Locale.ROOT);

                if (format.equals("jpg")) {
                    format = "jpeg";
                }

                // Decode the image to reject malformed image data.
                reader.read(0);

                return format;
            } finally {
                reader.dispose();
            }

        } catch (IOException | RuntimeException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or unsupported image file");
        }
    }
}
