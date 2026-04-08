package ru.mirea.recom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.recom.model.games.*;
import ru.mirea.recom.repository.*;

import java.util.List;

// GameService.java
@Service
@RequiredArgsConstructor
public class GameService {
    private final VideoGameRepository gameRepository;
    private final PlatformRepository platformRepository;
    private final CategoryRepository categoryRepository;
    private final GameGenreRepository genreRepository;
    private final SteamSpyTagRepository tagRepository;

    // GameService.java
    @Transactional
    public VideoGame createGame(GameCreateDto gameDto) {
        VideoGame game = new VideoGame();
        game.setName(gameDto.getName());
        game.setReleaseDate(gameDto.getReleaseDate());
        game.setEnglish(gameDto.getEnglish() != null ? gameDto.getEnglish() : true);
        game.setDeveloper(gameDto.getDeveloper());
        game.setPublisher(gameDto.getPublisher());
        game.setRequiredAge(gameDto.getRequiredAge() != null ? gameDto.getRequiredAge() : 0);
        game.setAchievements(gameDto.getAchievements() != null ? gameDto.getAchievements() : 0);
        game.setPositiveRatings(gameDto.getPositiveRatings() != null ? gameDto.getPositiveRatings() : 0);
        game.setNegativeRatings(gameDto.getNegativeRatings() != null ? gameDto.getNegativeRatings() : 0);
        game.setAveragePlaytime(gameDto.getAveragePlaytime() != null ? gameDto.getAveragePlaytime() : 0);
        game.setMedianPlaytime(gameDto.getMedianPlaytime() != null ? gameDto.getMedianPlaytime() : 0);
        game.setOwners(gameDto.getOwners() != null ? gameDto.getOwners() : "0-0");
        game.setPrice(gameDto.getPrice() != null ? gameDto.getPrice() : 0.0);

        // Обработка связей
        processPlatforms(game, gameDto.getPlatforms());
        processCategories(game, gameDto.getCategories());
        processGenres(game, gameDto.getGenres());
        processTags(game, gameDto.getTags());

        return gameRepository.save(game);
    }

    private void processPlatforms(VideoGame game, List<String> platformNames) {
        if (platformNames == null) return;
        platformNames.forEach(name -> {
            Platform platform = platformRepository.findByName(name)
                    .orElseGet(() -> {
                        Platform newPlatform = new Platform();
                        newPlatform.setName(name);
                        return platformRepository.save(newPlatform);
                    });
            game.getPlatforms().add(platform);
        });
    }
    private void processCategories(VideoGame game, List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) return;

        categoryNames.forEach(name -> {
            if (name != null && !name.trim().isEmpty()) {
                String normalizedName = name.trim();
                Category category = categoryRepository.findByName(normalizedName)
                        .orElseGet(() -> {
                            Category newCategory = new Category();
                            newCategory.setName(normalizedName);
                            return categoryRepository.save(newCategory);
                        });
                game.getCategories().add(category);
            }
        });
    }

    private void processGenres(VideoGame game, List<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) return;

        genreNames.forEach(name -> {
            if (name != null && !name.trim().isEmpty()) {
                String normalizedName = name.trim();
                GameGenre genre = genreRepository.findByName(normalizedName)
                        .orElseGet(() -> {
                            GameGenre newGenre = new GameGenre();
                            newGenre.setName(normalizedName);
                            return genreRepository.save(newGenre);
                        });
                game.getGenres().add(genre);
            }
        });
    }

    private void processTags(VideoGame game, List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) return;

        tagNames.forEach(name -> {
            if (name != null && !name.trim().isEmpty()) {
                String normalizedName = name.trim();
                SteamSpyTag tag = tagRepository.findByName(normalizedName)
                        .orElseGet(() -> {
                            SteamSpyTag newTag = new SteamSpyTag();
                            newTag.setName(normalizedName);
                            return tagRepository.save(newTag);
                        });
                game.getSteamspyTags().add(tag);
            }
        });
    }

    @Transactional
    public void deleteGame(Integer id) {
        gameRepository.deleteById(id);
    }
}