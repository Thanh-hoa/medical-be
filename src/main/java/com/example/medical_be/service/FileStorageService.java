package com.example.medical_be.service;

import com.example.medical_be.dto.res.FileUploadInfo;
import com.example.medical_be.exception.ApplicationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload-dir:uploads/photos}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    public FileUploadInfo store(MultipartFile file) {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = StringUtils.getFilenameExtension(originalFilename);

        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new ApplicationException("file.invalid_type");
        }

        String filename = UUID.randomUUID() + "." + extension.toLowerCase();
        try {
            Files.copy(file.getInputStream(), this.uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ApplicationException("file.upload_failed");
        }

        String filePath = "http://localhost:8080/uploads/photos/" + filename;
        return new FileUploadInfo(filePath, originalFilename, extension.toLowerCase());
    }
}