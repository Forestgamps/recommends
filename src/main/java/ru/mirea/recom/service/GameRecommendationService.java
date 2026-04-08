package ru.mirea.recom.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.recom.model.User;
import ru.mirea.recom.model.games.RecommendationHistory;
import ru.mirea.recom.model.games.VideoGame;
import ru.mirea.recom.repository.RecommendationHistoryRepository;
import ru.mirea.recom.repository.UserRepository;
import ru.mirea.recom.repository.VideoGameRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GameRecommendationService {
    private final VideoGameRepository gameRepository;
    private final GameVectorizer vectorizer;

    // Весовые коэффициенты для разных типов рекомендаций
    private static final double SIMILARITY_THRESHOLD = 0.4;
    private static final int POPULAR_GAMES_LIMIT = 100;


    @Autowired
    private RecommendationHistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void saveRecommendationHistory(String username, String type, List<VideoGame> games) {
        System.out.println("Saving history for: " + username); // Логирование
        System.out.println("Recommendation type: " + type);
        System.out.println("Games count: " + games.size());

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        RecommendationHistory history = new RecommendationHistory();
        history.setUser(user);
        history.setRecommendationType(type);
        history.setContentIds(games.stream()
                .map(VideoGame::getSteamAppId)
                .collect(Collectors.toList()));

        historyRepository.save(history);
        System.out.println("History saved with ID: " + history.getId()); // Логирование
    }
    /**
     * Рекомендации по похожим играм
     */
    @Cacheable(value = "gameRecommendations", key = "{#gameId, #limit}")
    public List<VideoGame> recommendSimilarGames(Integer gameId, int limit, String username) {
        VideoGame targetGame = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found with id: " + gameId));

        Map<String, Double> targetVector = vectorizer.vectorize(targetGame);
        List<String> targetGenres = targetGame.getGenres().stream()
                .map(g -> g.getName().toLowerCase())
                .collect(Collectors.toList());

        List<VideoGame> result =gameRepository.findAll().stream()
                .filter(game -> !game.getSteamAppId().equals(gameId))
                .filter(game -> hasCommonGenres(game, targetGenres))
                .map(game -> new ScoredGame(
                        game,
                        calculateEnhancedSimilarity(targetVector, targetGame, game)
                ))
                .sorted(Comparator.comparingDouble(ScoredGame::getScore).reversed())
                .limit(limit * 2L) // Берем больше для последующей фильтрации
                .filter(scoredGame -> scoredGame.getScore() >= SIMILARITY_THRESHOLD)
                .limit(limit)
                .map(ScoredGame::getGame)
                .collect(Collectors.toList());

        if (username != null) {
            saveRecommendationHistory(username, "similar-games", result);
        }

        return result;
    }

    /**
     * Персональные рекомендации на основе игр пользователя
     */
    @Cacheable(value = "userGameRecommendations", key = "{#playedGameIds, #limit}")
    public List<VideoGame> recommendForUser(List<Integer> playedGameIds, int limit, String username) {
        if (playedGameIds == null || playedGameIds.isEmpty()) {
            return recommendPopularGames(limit);
        }

        List<VideoGame> playedGames = gameRepository.findAllById(playedGameIds);
        Set<Integer> watchedGames = getWatchedGames(username);
        if (playedGames.isEmpty()) {
            return recommendPopularGames(limit);
        }

        // 1. Собираем объединенный вектор предпочтений
        Map<String, Double> combinedVector = new HashMap<>();
        Set<String> commonGenres = new HashSet<>();

        for (VideoGame game : playedGames) {
            Map<String, Double> gameVector = vectorizer.vectorize(game);
            gameVector.forEach((key, value) ->
                    combinedVector.merge(key, value, Double::sum)
            );

            // Собираем общие жанры
            if (commonGenres.isEmpty()) {
                commonGenres.addAll(game.getGenres().stream()
                        .map(g -> g.getName().toLowerCase())
                        .collect(Collectors.toSet()));
            } else {
                commonGenres.retainAll(game.getGenres().stream()
                        .map(g -> g.getName().toLowerCase())
                        .collect(Collectors.toSet()));
            }
        }

        // 2. Нормализуем вектор
        normalizeVector(combinedVector);

        List<VideoGame> result = gameRepository.findAll().stream()
                .filter(game -> !playedGameIds.contains(game.getSteamAppId()))
                .filter(game -> hasCommonGenres(game, commonGenres))
                .filter(game -> !watchedGames.contains(game.getSteamAppId()))
                .map(game -> new ScoredGame(
                        game,
                        calculateEnhancedSimilarity(combinedVector, playedGames.get(0), game)
                ))
                .sorted(Comparator.comparingDouble(ScoredGame::getScore).reversed())
                .limit(limit)
                .map(ScoredGame::getGame)
                .collect(Collectors.toList());

        if (username != null) {
            saveRecommendationHistory(username, "user-games", result);
        }
        // 3. Получаем рекомендации
        return result;
    }

    /**
     * Популярные игры с фильтрацией по качеству
     */
    @Cacheable(value = "popularGames", key = "#limit")
    public List<VideoGame> recommendPopularGames(int limit) {
        // Берем топ N популярных игр для выборки
        List<VideoGame> candidates = gameRepository.findPopularGames(PageRequest.of(0, POPULAR_GAMES_LIMIT));

        // Фильтруем по рейтингу и количеству оценок
        return candidates.stream()
                .filter(game -> game.getPositiveRatings() > 100) // Достаточное количество оценок
                .filter(game -> {
                    double totalRatings = game.getPositiveRatings() + game.getNegativeRatings();
                    double positiveRatio = game.getPositiveRatings() / totalRatings;
                    return positiveRatio > 0.7; // Минимальный порог качества
                })
                .sorted(Comparator.comparingDouble((VideoGame game) -> {
                    // Сортировка по "репутации" (формула Вильсона)
                    double positive = game.getPositiveRatings();
                    double negative = game.getNegativeRatings();
                    double total = positive + negative;
                    return (positive + 1.9208) / total -
                            1.96 * Math.sqrt((positive * negative) / total + 0.9604) / total;
                }).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Set<Integer> getWatchedGames(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return historyRepository.findByUserAndRecommendationType(user, "watched-game")
                .stream()
                .flatMap(h -> h.getContentIds().stream())
                .collect(Collectors.toSet());
    }

    /**
     * Улучшенный расчет схожести с учетом дополнительных факторов
     */
    private double calculateEnhancedSimilarity(Map<String, Double> targetVector,
                                               VideoGame targetGame,
                                               VideoGame candidateGame) {
        // Базовое косинусное сходство
        Map<String, Double> candidateVector = vectorizer.vectorize(candidateGame);
        double similarity = cosineSimilarity(targetVector, candidateVector);

        // Дополнительные факторы
        double genreBonus = calculateGenreBonus(targetGame, candidateGame);
        double platformBonus = calculatePlatformBonus(targetGame, candidateGame);
        double ratingBonus = calculateRatingBonus(candidateGame);

        // Итоговый вес (можно настроить коэффициенты)
        return similarity * 0.7 + genreBonus * 0.15 + platformBonus * 0.1 + ratingBonus * 0.05;
    }

    /**
     * Бонус за совпадение жанров
     */
    private double calculateGenreBonus(VideoGame target, VideoGame candidate) {
        Set<String> targetGenres = target.getGenres().stream()
                .map(g -> g.getName().toLowerCase())
                .collect(Collectors.toSet());

        long matches = candidate.getGenres().stream()
                .map(g -> g.getName().toLowerCase())
                .filter(targetGenres::contains)
                .count();

        return (double) matches / targetGenres.size();
    }

    /**
     * Бонус за совпадение платформ
     */
    private double calculatePlatformBonus(VideoGame target, VideoGame candidate) {
        if (target.getPlatforms().isEmpty() || candidate.getPlatforms().isEmpty()) {
            return 0;
        }

        Set<String> targetPlatforms = target.getPlatforms().stream()
                .map(p -> p.getName().toLowerCase())
                .collect(Collectors.toSet());

        long matches = candidate.getPlatforms().stream()
                .map(p -> p.getName().toLowerCase())
                .filter(targetPlatforms::contains)
                .count();

        return (double) matches / targetPlatforms.size();
    }

    /**
     * Бонус за рейтинг игры
     */
    private double calculateRatingBonus(VideoGame game) {
        if (game.getPositiveRatings() == null || game.getNegativeRatings() == null) {
            return 0;
        }

        double total = game.getPositiveRatings() + game.getNegativeRatings();
        if (total == 0) return 0;

        double ratio = game.getPositiveRatings() / total;
        // Нормализация к 0-1
        return (ratio - 0.5) * 2;
    }

    /**
     * Проверка на общие жанры
     */
    private boolean hasCommonGenres(VideoGame game, Collection<String> targetGenres) {
        if (targetGenres.isEmpty()) return true;

        return game.getGenres().stream()
                .anyMatch(g -> targetGenres.contains(g.getName().toLowerCase()));
    }

    /**
     * Косинусная мера сходства
     */
    private double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> commonKeys = new HashSet<>(v1.keySet());
        commonKeys.retainAll(v2.keySet());

        double dotProduct = commonKeys.stream()
                .mapToDouble(k -> v1.get(k) * v2.get(k))
                .sum();

        double v1Norm = Math.sqrt(v1.values().stream().mapToDouble(v -> v * v).sum());
        double v2Norm = Math.sqrt(v2.values().stream().mapToDouble(v -> v * v).sum());

        return (v1Norm == 0 || v2Norm == 0) ? 0 : dotProduct / (v1Norm * v2Norm);
    }

    /**
     * Нормализация вектора
     */
    private void normalizeVector(Map<String, Double> vector) {
        double norm = Math.sqrt(vector.values().stream()
                .mapToDouble(v -> v * v).sum());

        if (norm > 0) {
            vector.replaceAll((k, v) -> v / norm);
        }
    }

    @Getter
    @AllArgsConstructor
    private static class ScoredGame {
        private VideoGame game;
        private double score;
    }
}