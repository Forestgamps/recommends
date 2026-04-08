// GameCreateDto.java
package ru.mirea.recom.model.games;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class GameCreateDto {
    private String name;
    private LocalDate releaseDate;
    private Boolean english;
    private String developer;
    private String publisher;
    private Integer requiredAge;
    private Integer achievements;
    private Integer positiveRatings;
    private Integer negativeRatings;
    private Integer averagePlaytime;
    private Integer medianPlaytime;
    private String owners;
    private Double price;
    private List<String> platforms;
    private List<String> categories;
    private List<String> genres;
    private List<String> tags;
}