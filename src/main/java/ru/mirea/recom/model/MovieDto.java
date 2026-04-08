package ru.mirea.recom.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieDto {
    private Integer id;
    private String title;
    private String releaseDate;
    
    public MovieDto(Movie movie) {
        this.id = movie.getId();
        this.title = movie.getTitle();
        this.releaseDate = movie.getReleaseDate() != null ? 
            movie.getReleaseDate().toString() : null;
    }
}