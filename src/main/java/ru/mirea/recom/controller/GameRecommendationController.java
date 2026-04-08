package ru.mirea.recom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.mirea.recom.model.games.GameCard;
import ru.mirea.recom.model.games.VideoGame;
import ru.mirea.recom.model.games.VideoGameMedia;
import ru.mirea.recom.repository.VideoGameMediaRepository;
import ru.mirea.recom.service.GameRecommendationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/games/recommendations")
@RequiredArgsConstructor
public class GameRecommendationController {
    private final GameRecommendationService recommendationService;
    private final VideoGameMediaRepository mediaRepository;

    @GetMapping("/similar/{gameId}")
    public List<GameCard> getSimilarGames(
            @PathVariable Integer gameId,
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal String username) {
        System.out.println(username);
        username = "123";
        return recommendationService.recommendSimilarGames(gameId, limit, username).stream()
                .map(game -> {
                    VideoGameMedia media = mediaRepository.findById(game.getSteamAppId()).orElse(null);
                    return new GameCard(game, media);
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/for-user")
    public List<GameCard> getRecommendationsForUser(
            @RequestParam List<Integer> playedGames,
            @RequestParam(defaultValue = "6") int limit,
            @AuthenticationPrincipal String username) {

        //String username = userDetails.getUsername();
        List<VideoGame> recommended = recommendationService.recommendForUser(playedGames, limit, username);

        return recommended.stream()
                .map(game -> {
                    VideoGameMedia media = mediaRepository.findById(game.getSteamAppId()).orElse(null);
                    return new GameCard(game, media); // Используйте существующий GameCard
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/popular")
    public List<VideoGame> getPopularGames(
            @RequestParam(defaultValue = "6") int limit) {
        return recommendationService.recommendPopularGames(limit);
    }
}