package ru.mirea.recom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.recom.model.games.GameDto;
import ru.mirea.recom.model.games.VideoGameMedia;
import ru.mirea.recom.repository.VideoGameMediaRepository;
import ru.mirea.recom.repository.VideoGameRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    private final VideoGameRepository gameRepository;
    private final VideoGameMediaRepository mediaRepository;

    @GetMapping("/search")
    public List<GameDto> searchGames(@RequestParam String query) {
        return gameRepository.findByNameContainingIgnoreCase(query).stream()
                .map(game -> {
                    VideoGameMedia media = mediaRepository.findById(game.getSteamAppId()).orElse(null);
                    return new GameDto(game, media);
                })
                .collect(Collectors.toList());
    }
}