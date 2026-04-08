package ru.mirea.recom.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.recom.model.MovieDto;
import ru.mirea.recom.repository.MovieRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieRepository movieRepository;

    @GetMapping("/search")
    public List<MovieDto> searchMovies(@RequestParam String query) {
        return movieRepository.findByTitleContainingIgnoreCase(query)
                .stream()
                .map(MovieDto::new)
                .collect(Collectors.toList());
    }


}