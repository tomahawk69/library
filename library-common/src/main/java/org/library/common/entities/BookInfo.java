package org.library.common.entities;

import java.util.Collections;
import java.util.Map;

public class BookInfo {
    private String title;
    private String language;
    private String sourceLanguage;
    private Map<String, Integer> genres;
    private Map<String, Integer> sequences;
    private String annotation;
    private Integer year;

    public String getTitle() {
        return title;
    }

    public BookInfo setTitle(String title) {
        this.title = title;
        return this;
    }

    public String[] getGenresList() {
        return genres.keySet().toArray(new String[genres.keySet().size()]);
    }

    public Map<String, Integer> getGenres() {
        return genres;
    }

    public BookInfo setGenres(Map<String, Integer> genres) {
        this.genres = Collections.unmodifiableMap(genres);
        return this;
    }

    public Map<String, Integer> getSequences() {
        return sequences;
    }

    public BookInfo setSequences(Map<String, Integer> sequences) {
        this.sequences = Collections.unmodifiableMap(sequences);
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public BookInfo setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public BookInfo setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
        return this;
    }

    public String getAnnotation() {
        return annotation;
    }

    public BookInfo setAnnotation(String annotation) {
        this.annotation = annotation;
        return this;
    }

    public Integer getYear() {
        return year;
    }

    public BookInfo setYear(Integer year) {
        this.year = year;
        return this;
    }


    @Override
    public String toString() {
        return "BookInfo{" +
                "title='" + title + '\'' +
                ", language='" + language + '\'' +
                ", sourceLanguage='" + sourceLanguage + '\'' +
                ", genres=" + genres +
                ", sequences=" + sequences +
                ", annotation='" + annotation + '\'' +
                ", year=" + year +
                '}';
    }
}
