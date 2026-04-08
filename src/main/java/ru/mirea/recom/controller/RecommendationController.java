package ru.mirea.recom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.mirea.recom.model.Movie;
import ru.mirea.recom.model.MovieCard;
import ru.mirea.recom.service.RecommendationService;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/similar/{movieId}")
    public ResponseEntity<List<String>> getSimilarMovies(
            @PathVariable Integer movieId,
            @RequestParam(defaultValue = "5") int limit)
    {
        List<Movie> FIlmsList = recommendationService.recommendSimilarMovies(movieId, limit);
        List<String> FilmTitles = new ArrayList<>();
        for (Movie movik: FIlmsList)
        {
            FilmTitles.add(movik.getTitle());
        }
        //recommendationService.recommendSimilarMovies(movieId, limit);
        return ResponseEntity.ok(FilmTitles

        );
    }

//    @GetMapping("/multi")
//    public List<String> getMultiRecommendations(
//            @RequestParam List<Integer> movieIds,
//            @RequestParam(defaultValue = "5") int limit
//    )
//    {
//        List<Movie> FIlmsList = recommendationService.recommendBasedOnMultipleMovies(movieIds, limit);
//        List<String> FilmTitles = new ArrayList<>();
//        for (Movie movik: FIlmsList)
//        {
//            FilmTitles.add(movik.getTitle());
//        }
//        return FilmTitles;
//    }

    @GetMapping("/multi")
    public List<MovieCard> getMultiRecommendations(
            @RequestParam List<Integer> movieIds,
            @RequestParam(defaultValue = "5") int limit
    )
    {
        List<Movie> FIlmsList = recommendationService.recommendBasedOnMultipleMovies(movieIds, limit);
        List<MovieCard> FilmTitles = new ArrayList<>();
        for (Movie movik: FIlmsList)
        {
            MovieCard card = new MovieCard(movik);
            FilmTitles.add(card);
        }
        return FilmTitles;
    }

    @GetMapping("/movies")
    public String showMoviesPage(Model model) {
        // Можно добавить данные в модель
        model.addAttribute("pageTitle", "Подбор фильмов");
        return "movies"; // Это должно соответствовать имени файла БЕЗ .html
    }
}