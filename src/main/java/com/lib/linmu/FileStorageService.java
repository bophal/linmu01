package com.lib.linmu;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;                    // ✅ fixed — was jakarta.persistence.criteria.Path
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;  // ✅ fixed — was jakarta.annotation.Resource
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final String uploadDir = "uploads/pdfs/";

    public String storePdfFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }

    // Delete old PDF from disk
    public void deletePdfFile(String filename) {
        if (filename == null || filename.isBlank()) return;

        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Warning: Could not delete old PDF: " + filename + " — " + e.getMessage());
        }
    }

    // Replace old PDF — deletes old, stores new, returns new filename
    public String replacePdfFile(String oldFilename, MultipartFile newFile) throws IOException {
        deletePdfFile(oldFilename);
        return storePdfFile(newFile);
    }

    public Resource loadPdfFile(String filename) throws MalformedURLException {
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists()) {
            return resource;
        } else {
            throw new RuntimeException("File not found: " + filename);
        }
    }
}