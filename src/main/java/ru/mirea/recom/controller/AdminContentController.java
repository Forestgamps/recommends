package ru.mirea.recom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.mirea.recom.model.Movie;
import ru.mirea.recom.model.MovieCreateDto;
import ru.mirea.recom.model.MovieDto;
import ru.mirea.recom.model.games.GameCreateDto;
import ru.mirea.recom.model.games.GameDto;
import ru.mirea.recom.model.games.VideoGame;
import ru.mirea.recom.service.GameService;
import ru.mirea.recom.service.MovieService;

// AdminContentController.java
@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
public class AdminContentController {
    private final MovieService movieService;
    private final GameService gameService;

    @PostMapping("/movies")
    public ResponseEntity<MovieDto> createMovie(@RequestBody MovieCreateDto movieDto) {
        Movie movie = movieService.createMovie(movieDto);
        return ResponseEntity.ok(new MovieDto(movie));
    }

    @PostMapping("/games")
    public ResponseEntity<GameDto> createGame(@RequestBody GameCreateDto gameDto) {
        VideoGame game = gameService.createGame(gameDto);
        return ResponseEntity.ok(new GameDto(game, null)); // Медиа можно добавить позже
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Integer id) {
        movieService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/games/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Integer id) {
        gameService.deleteGame(id);
        return ResponseEntity.noContent().build();
    }
}