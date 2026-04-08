package ru.mirea.recom.model;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class MovieCard {
    private String title;
    private String releaseDate;
    private String desc;
    private Double voteAverage; // Новое поле
    private List<String> genres; // Новое поле
    private String posterPath;

    public MovieCard(Movie movie) {
        this.title = movie.getTitle();
        this.desc = movie.getOverview();
        this.releaseDate = movie.getReleaseDate() != null ?
                movie.getReleaseDate().toString() : null;
        this.voteAverage = movie.getVoteAverage();
        this.posterPath = movie.getPosterPath();
        this.genres = movie.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toList());
    }
}
