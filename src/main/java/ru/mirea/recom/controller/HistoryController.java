package ru.mirea.recom.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import ru.mirea.recom.model.User;
import ru.mirea.recom.model.games.RecommendationHistory;
import ru.mirea.recom.model.games.VideoGame;
import ru.mirea.recom.model.games.VideoGameMedia;
import ru.mirea.recom.repository.RecommendationHistoryRepository;
import ru.mirea.recom.repository.UserRepository;
import ru.mirea.recom.repository.VideoGameMediaRepository;
import ru.mirea.recom.repository.VideoGameRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {
    private final RecommendationHistoryRepository historyRepository;
    private final VideoGameRepository gameRepository;
    private final VideoGameMediaRepository mediaRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Transactional
    public ResponseEntity<?> getUserHistory() {
        // Получаем аутентификацию из контекста безопасности
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(403).body("Доступ запрещен");
        }

        String username = authentication.getName();
        System.out.println("Getting history for user: " + username); // Отладочный вывод

        // Получаем пользователя по имени
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<RecommendationHistory> history = historyRepository.findByUserOrderByCreatedAtDesc(user);
        System.out.println(history.toString());
        // Используем новый DTO с детализированной информацией
        List<HistoryItemDto> historyDtos = history.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(historyDtos);
    }

    private HistoryItemDto convertToDto(RecommendationHistory history) {
        return new HistoryItemDto(
                history.getId(),
                history.getRecommendationType(),
                history.getCreatedAt(),
                history.getContentIds().stream()
                        .map(gameId -> {
                            // Ищем игру по SteamAppId
                            String name = gameRepository.findBySteamAppId(gameId)
                                    .map(VideoGame::getName)
                                    .orElse("Неизвестная игра (ID: " + gameId + ")");

                            // Ищем медиа по SteamAppId
                            String image = mediaRepository.findBySteamAppId(gameId)
                                    .map(VideoGameMedia::getHeaderImage)
                                    .orElse(null);

                            return new ContentItemDto(gameId, name, image);
                        })
                        .collect(Collectors.toList())
        );
    }

    @DeleteMapping
    public ResponseEntity<?> clearUserHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        long deletedCount = historyRepository.deleteByUser(user);

        return ResponseEntity.ok().body(Map.of(
                "message", "История успешно очищена",
                "deletedCount", deletedCount
        ));
    }

    @PostMapping("/watch")
    public ResponseEntity<?> markAsWatched(@RequestBody WatchRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ запрещен");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        RecommendationHistory history = new RecommendationHistory();
        history.setUser(user);
        history.setRecommendationType("watched-" + request.getContentType());
        history.setContentIds(List.of(request.getContentId()));
        history.setCreatedAt(LocalDateTime.now());

        historyRepository.save(history);

        return ResponseEntity.ok().build();
    }

    @Data
    static class WatchRequest {
        private String contentType; // "movie" или "game"
        private Integer contentId;
    }

    @Data
    @AllArgsConstructor
    public static class HistoryItemDto {
        private Long id;
        private String recommendationType;
        private LocalDateTime createdAt;
        @JsonProperty("content_details") // Добавьте эту аннотацию
        private List<ContentItemDto> contentDetails;
    }

    @Data
    @AllArgsConstructor
    public static class ContentItemDto {
        private Integer id;
        private String name;
        private String image;
    }
}