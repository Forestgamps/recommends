package ru.mirea.recom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mirea.recom.model.Movie;
import ru.mirea.recom.model.MovieCast;
import ru.mirea.recom.repository.GenreRepository;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MovieVectorizer {
    private final GenreRepository genreRepository;

    // Веса для разных признаков (можно настраивать)
    private static final double GENRE_WEIGHT = 0.4;
    private static final double CAST_WEIGHT = 0.3;
    private static final double CREW_WEIGHT = 0.2;
    private static final double OVERVIEW_WEIGHT = 0.1;

    public Map<String, Double> vectorize(Movie movie) {
        Map<String, Double> vector = new HashMap<>();

        // 1. Жанры
        movie.getGenres().forEach(genre ->
                vector.merge("genre_" + genre.getName(), GENRE_WEIGHT, Double::sum));

        // 2. Актёры (топ-5 по order)
        movie.getCast().stream()
                .sorted(Comparator.comparing(MovieCast::getOrder))
                .limit(5)
                .forEach(cast ->
                        vector.merge("cast_" + cast.getPerson().getName(), CAST_WEIGHT, Double::sum));

        // 3. Режиссёры
        movie.getCrew().stream()
                .filter(crew -> "Directing".equals(crew.getDepartment()))
                .forEach(crew ->
                        vector.merge("director_" + crew.getPerson().getName(), CREW_WEIGHT, Double::sum));

        // 4. Ключевые слова из описания (упрощённо)
        if (movie.getOverview() != null) {
            Arrays.stream(movie.getOverview().split("\\s+"))
                    .filter(word -> word.length() > 3)
                    .limit(20)
                    .forEach(word ->
                            vector.merge("word_" + word.toLowerCase(), OVERVIEW_WEIGHT, Double::sum));
        }

        return vector;
    }
}