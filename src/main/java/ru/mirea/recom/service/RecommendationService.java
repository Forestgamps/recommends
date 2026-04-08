package ru.mirea.recom.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.recom.model.Movie;
import ru.mirea.recom.repository.MovieRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final MovieRepository movieRepository;
    private final MovieVectorizer vectorizer;

    public List<Movie> recommendSimilarMovies(Integer movieId, int limit) {
        Movie targetMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        Map<String, Double> targetVector = vectorizer.vectorize(targetMovie);

        return movieRepository.findAll().stream()
                .filter(m -> !m.getId().equals(movieId)) // Исключаем исходный фильм
                .map(m -> new ScoredMovie(m, cosineSimilarity(targetVector, vectorizer.vectorize(m))))
                .sorted(Comparator.comparingDouble(ScoredMovie::getScore).reversed())
                .limit(limit)
                .map(ScoredMovie::getMovie)
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        // Общие ключи
        Set<String> commonKeys = new HashSet<>(v1.keySet());
        commonKeys.retainAll(v2.keySet());

        // Вычисляем числитель
        double dotProduct = commonKeys.stream()
                .mapToDouble(k -> v1.get(k) * v2.get(k))
                .sum();

        // Вычисляем знаменатель
        double v1Norm = Math.sqrt(v1.values().stream().mapToDouble(v -> v * v).sum());
        double v2Norm = Math.sqrt(v2.values().stream().mapToDouble(v -> v * v).sum());

        return dotProduct / (v1Norm * v2Norm);
    }

    @Transactional
    public List<Movie> recommendBasedOnMultipleMovies(List<Integer> movieIds, int limit) {
        // 1. Получаем целевые фильмы
        List<Movie> targetMovies = movieRepository.findAllById(movieIds);

        // 2. Создаём общий вектор ("сумму" векторов всех фильмов)
        Map<String, Double> combinedVector = new HashMap<>();
        for (Movie movie : targetMovies) {
            Map<String, Double> movieVector = vectorizer.vectorize(movie);
            movieVector.forEach((key, value) ->
                    combinedVector.merge(key, value, Double::sum)
            );
        }
        System.out.println(combinedVector);
        // 3. Нормализуем вектор
        normalizeVector(combinedVector);
        System.out.println(combinedVector);
        // 4. Ищем похожие
        return movieRepository.findAll().stream()
                .filter(m -> !movieIds.contains(m.getId())) // Исключаем исходные
                .map(m -> new ScoredMovie(m, cosineSimilarity(combinedVector, vectorizer.vectorize(m))))
                .sorted(Comparator.comparingDouble(ScoredMovie::getScore).reversed())
                .limit(limit)
                .map(ScoredMovie::getMovie)
                .collect(Collectors.toList());
    }

    // Нормализация вектора
    private void normalizeVector(Map<String, Double> vector) {
        double norm = Math.sqrt(vector.values().stream()
                .mapToDouble(v -> v * v).sum());
        vector.replaceAll((k, v) -> v / norm);
    }

    @Getter
    @AllArgsConstructor
    private static class ScoredMovie {
        private Movie movie;
        private double score;

    }


}