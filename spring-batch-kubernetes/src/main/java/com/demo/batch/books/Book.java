package com.demo.batch.books;

import lombok.Data;

import java.time.LocalDate;

@Data
public final class Book {
    private Long id;
    private String title;
    private String authors;
    private Float averageRating;
    private String isbn;
    private String isbn13;
    private String languageCode;
    private Integer numPages;
    private Long ratingsCount;
    private Long textReviewsCount;
    private LocalDate publicationDate;
    private String publisher;
}
