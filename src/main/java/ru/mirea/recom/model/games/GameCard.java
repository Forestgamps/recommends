package ru.mirea.recom.model.games;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class GameCard {
    private Integer id;
    private String name;
    private String releaseDate;
    private String developer;
    private String description;
    private Double rating;
    private Double price;
    private String headerImage;
    private List<String> genres;
    private List<String> platforms;

    public GameCard(VideoGame game, VideoGameMedia media) {
        this.id = game.getSteamAppId();
        this.name = game.getName();
        this.releaseDate = game.getReleaseDate() != null ? 
            game.getReleaseDate().toString() : null;
        this.developer = game.getDeveloper();
        this.price = game.getPrice();
        this.headerImage = media != null ? media.getHeaderImage() : "https://cdn.cloudflare.steamstatic.com/steam/apps/" + game.getSteamAppId() + "/header.jpg";
        this.genres = game.getGenres().stream()
                .map(GameGenre::getName)
                .collect(Collectors.toList());
        this.platforms = game.getPlatforms().stream()
                .map(Platform::getName)
                .collect(Collectors.toList());
    }
}