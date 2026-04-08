package ru.mirea.recom.model.games;

import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class GameDto {
    private final Integer id;
    private final String name;
    private final String releaseDate;
    private final String developer;
    private final String headerImage;

    public GameDto(VideoGame game, VideoGameMedia media) {
        this.id = game.getSteamAppId();
        this.name = game.getName();
        this.releaseDate = game.getReleaseDate() != null ?
                game.getReleaseDate().format(DateTimeFormatter.ISO_DATE) : null;
        this.developer = game.getDeveloper();
        this.headerImage = media != null ? media.getHeaderImage() : null;
    }
}