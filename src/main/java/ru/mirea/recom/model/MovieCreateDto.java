// MovieCreateDto.java
package ru.mirea.recom.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MovieCreateDto {
    private String title;
    private String originalTitle;
    private String overview;
    private String tagline;
    private String homepage;
    private String originalLanguage;
    private LocalDate releaseDate;
    private Double popularity;
    private Long budget;
    private Long revenue;
    private Integer runtime;
    private Double voteAverage;
    private Integer voteCount;
    private String status;
    private List<Long> genreIds;
}