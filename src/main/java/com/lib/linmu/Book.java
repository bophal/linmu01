package com.lib.linmu;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String isbn;
    private int quantity;
    private String coverUrl;
    public String getCoverUrl() {
		return coverUrl;
	}
 // Store the relative path to the PDF file
    @Column(name = "pdf_file_path")
    private String pdfFilePath;
    
    @Column(name = "cover_image_path")
    private String coverImagePath;   // book cover image

   // @Column(name = "pdf_file_path")
    //private String pdfFilePath;      // linked PDF

	public String getPdfFilePath() {
		return pdfFilePath;
	}
	public String getCoverImagePath() {
		return coverImagePath;
	}
	public void setCoverImagePath(String coverImagePath) {
		this.coverImagePath = coverImagePath;
	}
	public void setPdfFilePath(String pdfFilePath) {
		this.pdfFilePath = pdfFilePath;
	}
	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}
	public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public int getQuantity() { return quantity; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

}
