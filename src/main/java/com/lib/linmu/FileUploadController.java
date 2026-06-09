package com.lib.linmu;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
 
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")

public class FileUploadController {
	@Autowired
    private BookRepository bookRepository;
 
    // Upload folder — saves inside project's static folder
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";
 
    // Upload cover image for a book
    @PostMapping("/cover/{bookId}")
    public ResponseEntity<?> uploadCover(
            @PathVariable Long bookId,
            @RequestParam("file") MultipartFile file) {
 
        // Check book exists
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "រកមិនឃើញសៀវភៅ!"));
        }
 
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("message", "សូមជ្រើសរើសរូបភាពប្រភេទ JPG, PNG ឬ WEBP!"));
        }
 
        // Validate file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("message", "ទំហំរូបភាពមិនត្រូវលើស 5MB!"));
        }
 
        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
 
            // Delete old cover if exists
            Book book = bookOpt.get();
            if (book.getCoverUrl() != null) {
                String oldFile = book.getCoverUrl().replace("/uploads/", UPLOAD_DIR);
                try { Files.deleteIfExists(Paths.get(oldFile)); } catch (Exception ignored) {}
            }
 
            // Generate unique filename
            String ext = getExtension(file.getOriginalFilename());
            String filename = "book_" + bookId + "_" + System.currentTimeMillis() + ext;
            Path filePath = uploadPath.resolve(filename);
 
            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
 
            // Update book coverUrl in database
            String coverUrl = "/uploads/" + filename;
            book.setCoverUrl(coverUrl);
            bookRepository.save(book);
 
            return ResponseEntity.ok(Map.of(
                "message", "បានបញ្ចូលរូបភាពដោយជោគជ័យ!",
                "coverUrl", coverUrl,
                "bookId", bookId
            ));
 
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "មានបញ្ហាក្នុងការរក្សាទុករូបភាព: " + e.getMessage()));
        }
    }
 
    // Upload cover when creating a new book (no ID yet)
    @PostMapping("/cover")
    public ResponseEntity<?> uploadTempCover(@RequestParam("file") MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("message", "ប្រភេទឯកសារមិនត្រឹមត្រូវ!"));
        }
 
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
 
            String ext = getExtension(file.getOriginalFilename());
            String filename = "temp_" + System.currentTimeMillis() + ext;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
 
            return ResponseEntity.ok(Map.of(
                "message", "បានបញ្ចូលរូបភាពដោយជោគជ័យ!",
                "coverUrl", "/uploads/" + filename
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("message", "Error: " + e.getMessage()));
        }
    }
 
    // Delete cover image
    @DeleteMapping("/cover/{bookId}")
    public ResponseEntity<?> deleteCover(@PathVariable Long bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("message", "រកមិនឃើញសៀវភៅ!"));
 
        Book book = bookOpt.get();
        if (book.getCoverUrl() != null) {
            try {
                String filePath = book.getCoverUrl().replace("/uploads/", UPLOAD_DIR);
                Files.deleteIfExists(Paths.get(filePath));
            } catch (Exception ignored) {}
            book.setCoverUrl(null);
            bookRepository.save(book);
        }
        return ResponseEntity.ok(Map.of("message", "បានលុបរូបភាពដោយជោគជ័យ!"));
    }
 
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

}
