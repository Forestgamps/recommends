// src/main/java/ru/mirea/recom/model/games/VideoGame.java
package ru.mirea.recom.model.games;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "video_games")
public class VideoGame {
    @Id
    @Column(name = "steam_app_id")
    private Integer steamAppId;

    private String name;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    private Boolean english;
    private String developer;
    private String publisher;

    @Column(name = "required_age")
    private Integer requiredAge;

    private Integer achievements;

    @Column(name = "positive_ratings")
    private Integer positiveRatings;

    @Column(name = "negative_ratings")
    private Integer negativeRatings;

    @Column(name = "average_playtime")
    private Integer averagePlaytime;

    @Column(name = "median_playtime")
    private Integer medianPlaytime;

    private String owners;
    private Double price;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "video_game_platform",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "platform_id"))
    private Set<Platform> platforms = new HashSet<>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "video_game_category",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "video_game_genre",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<GameGenre> genres = new HashSet<>();

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "video_game_steamspy_tag",
            joinColumns = @JoinColumn(name = "game_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<SteamSpyTag> steamspyTags = new HashSet<>();
}
