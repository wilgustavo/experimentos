package com.demo.batch.books;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class BookEntity {
    private Long id;
    private String title;
    private String authors;
    private String isbn;
    private LocalDate publication;
    private String publisher;

    public static BookEntity fromBook(Book book) {
        return new BookEntity(book.getId(), book.getTitle(), book.getAuthors(),
                book.getIsbn13(), book.getPublicationDate(), book.getPublisher());
    }

}
