package com.example.booklisting;

public class Book {

    /**
     * Title of the book
     */
    private final String title;
    /**
     * Author of the book
     */
    private final String author;
    /**
     * URL address of an image cover of the book
     */
    private final String imageUrl;
    /**
     * Price of the book
     */
    private final Double price;
    /**
     * Price of the book
     */
    private final String currency;
    /**
     * Country code of language
     */
    private final String language;
    /**
     * Url of the book
     */
    private String urlBook;

    public Book(String bookTitle, String authorName, String urlImageCover, Double bookPrice, String currency, String languageCode, String buyLink) {
        this.title = bookTitle;
        this.author = authorName;
        this.imageUrl = urlImageCover;
        this.price = bookPrice;
        this.currency = currency;
        this.language = languageCode;
        this.urlBook = buyLink;

    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Double getPrice() {
        return price;
    }

    public String getCurrency() {
        return currency;
    }

    public String getLanguage() {
        return language;
    }

    public String getUrlBook() {
        return urlBook;
    }
}
