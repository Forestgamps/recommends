package ru.mirea.recom.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class Movie {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "original_title")
    private String originalTitle;

    @Column(name = "overview", length = 2000)
    private String overview;

    @Column(name = "tagline", length = 500)
    private String tagline;

    @Column(name = "homepage")
    private String homepage;

    @Column(name = "original_language", length = 10)
    private String originalLanguage;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "popularity")
    private Double popularity;

    @Column(name = "budget")
    private Long budget;

    @Column(name = "revenue")
    private Long revenue;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "vote_average")
    private Double voteAverage;

    @Column(name = "vote_count")
    private Integer voteCount;

    @Column(name = "status")
    private String status;

    @Column(name = "poster_path")
    private String posterPath;

    @ManyToMany
    @JoinTable(
            name = "movies_genres",
            joinColumns = @JoinColumn(name = "movies_id"),
            inverseJoinColumns = @JoinColumn(name = "genres_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieCast> cast = new HashSet<>();

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MovieCrew> crew = new HashSet<>();


}
