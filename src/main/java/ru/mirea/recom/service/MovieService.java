package ru.mirea.recom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.recom.model.Genre;
import ru.mirea.recom.model.Movie;
import ru.mirea.recom.model.MovieCreateDto;
import ru.mirea.recom.repository.GenreRepository;
import ru.mirea.recom.repository.MovieRepository;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

// MovieService.java
@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;

    @Transactional
    public Movie createMovie(MovieCreateDto movieDto) {
        Movie movie = new Movie();
        movie.setTitle(movieDto.getTitle());
        movie.setOriginalTitle(movieDto.getOriginalTitle());
        movie.setOverview(movieDto.getOverview());
        movie.setTagline(movieDto.getTagline());
        movie.setHomepage(movieDto.getHomepage());
        movie.setOriginalLanguage(movieDto.getOriginalLanguage());
        movie.setReleaseDate(movieDto.getReleaseDate());
        movie.setPopularity(movieDto.getPopularity() != null ? movieDto.getPopularity() : 0.0);
        movie.setBudget(movieDto.getBudget() != null ? movieDto.getBudget() : 0L);
        movie.setRevenue(movieDto.getRevenue() != null ? movieDto.getRevenue() : 0L);
        movie.setRuntime(movieDto.getRuntime() != null ? movieDto.getRuntime() : 0);
        movie.setVoteAverage(movieDto.getVoteAverage() != null ? movieDto.getVoteAverage() : 0.0);
        movie.setVoteCount(movieDto.getVoteCount() != null ? movieDto.getVoteCount() : 0);
        movie.setStatus(movieDto.getStatus() != null ? movieDto.getStatus() : "Released");

        // Обработка жанров
        Set<Genre> genres = genreRepository.findAllById(
                movieDto.getGenreIds() != null ? movieDto.getGenreIds() : Collections.emptyList()
        ).stream().collect(Collectors.toSet());
        movie.setGenres(genres);

        return movieRepository.save(movie);
    }

    @Transactional
    public void deleteMovie(Integer id) {
        movieRepository.deleteById(id);
    }
}