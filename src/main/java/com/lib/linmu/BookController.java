package com.lib.linmu;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;               // ✅ correct
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;                 // ✅ correct
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;                       // ✅ correct
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/books")
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // ─────────────────────────────────────────────
    // Book list page
    // URL: /books/list
    // ─────────────────────────────────────────────
    @GetMapping("/list")                               // ✅ fixed — was "/books/list"
    public String bookList(Model model) {              // ✅ fixed import
        model.addAttribute("books", bookRepository.findAll());
        return "book-list";
    }

    // ─────────────────────────────────────────────
    // View PDF in browser — called when cover is clicked
    // URL: /books/{id}/view-pdf
    // ─────────────────────────────────────────────
    @GetMapping("/{id}/view-pdf")                      // ✅ kept only one viewPdf
    public ResponseEntity<Resource> viewPdf(@PathVariable Long id) {
        try {
            Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

            if (book.getPdfFilePath() == null || book.getPdfFilePath().isBlank()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = fileStorageService.loadPdfFile(book.getPdfFilePath());

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)    // ✅ fixed import
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ─────────────────────────────────────────────
    // Upload or replace PDF
    // URL: POST /books/{id}/upload-pdf
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/upload-pdf")
    public String uploadPdf(@PathVariable Long id,
                            @RequestParam("pdfFile") MultipartFile file,
                            RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please select a PDF file.");
            return "redirect:/books/" + id;
        }

        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".pdf")) {
            redirectAttributes.addFlashAttribute("error", "Only PDF files are allowed.");
            return "redirect:/books/" + id;
        }

        try {
            Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

            String oldPdfPath = book.getPdfFilePath();
            String newFilename;

            if (oldPdfPath != null && !oldPdfPath.isBlank()) {
                newFilename = fileStorageService.replacePdfFile(oldPdfPath, file);
                redirectAttributes.addFlashAttribute("success", "PDF replaced successfully!");
            } else {
                newFilename = fileStorageService.storePdfFile(file);
                redirectAttributes.addFlashAttribute("success", "PDF uploaded successfully!");
            }

            book.setPdfFilePath(newFilename);
            bookRepository.save(book);

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload PDF: " + e.getMessage());
        }

        return "redirect:/books/list";                 // ✅ redirect to list page
    }

    // ─────────────────────────────────────────────
    // Delete PDF only (keep the book)
    // URL: POST /books/{id}/delete-pdf
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/delete-pdf")
    public String deletePdf(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

            if (book.getPdfFilePath() == null || book.getPdfFilePath().isBlank()) {
                redirectAttributes.addFlashAttribute("error", "This book has no PDF to delete.");
                return "redirect:/books/list";
            }

            fileStorageService.deletePdfFile(book.getPdfFilePath());
            book.setPdfFilePath(null);
            bookRepository.save(book);

            redirectAttributes.addFlashAttribute("success", "PDF deleted successfully.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete PDF: " + e.getMessage());
        }

        return "redirect:/books/list";
    }

    // ─────────────────────────────────────────────
    // REST API endpoints
    // ─────────────────────────────────────────────
    @GetMapping
    @ResponseBody                                      // ✅ needed since class is @Controller not @RestController
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Book getBookById(@PathVariable Long id) {
        return bookRepository.findById(id).orElse(null);
    }

    @PostMapping
    @ResponseBody
    public Book addBook(@RequestBody Book book) {
        return bookRepository.save(book);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public Book updateBook(@PathVariable Long id, @RequestBody Book updated) {
        updated.setId(id);
        return bookRepository.save(updated);
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteBook(@PathVariable Long id) {
        bookRepository.deleteById(id);
        return "Deleted";
    }
}