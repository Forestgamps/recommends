package ru.mirea.recom.parcing;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.recom.model.games.*;
import ru.mirea.recom.repository.*;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class VideoGameDataParser {
    private static final int BATCH_SIZE = 500;
    private static final Pattern LIST_SEP = Pattern.compile("[\\t;]");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    private final VideoGameRepository videoGameRepo;
    private final PlatformRepository platformRepo;
    private final CategoryRepository categoryRepo;
    private final GameGenreRepository genreRepo;
    private final SteamSpyTagRepository tagRepo;
    private final EntityManager entityManager;

    public void parseAndSaveVideoGames(String filePath) {
        Map<String, Platform> platformCache = new HashMap<>();
        Map<String, Category> categoryCache = new HashMap<>();
        Map<String, GameGenre> genreCache = new HashMap<>();
        Map<String, SteamSpyTag> tagCache = new HashMap<>();

        int processed = 0;
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] header = reader.readNext();
            if (header != null && header[0].startsWith("\uFEFF")) {
                header[0] = header[0].substring(1);
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                try {
                    VideoGame game = parseGameLine(line, platformCache, categoryCache, genreCache, tagCache);
                    videoGameRepo.save(game);
                    processed++;

                    if (processed % BATCH_SIZE == 0) {
                        flushAndClearCaches(platformCache, categoryCache, genreCache, tagCache);
                        log.info("Processed {} games", processed);
                    }
                } catch (Exception e) {
                    log.error("Error processing line {}: {}", Arrays.toString(line), e.getMessage());
                }
            }

            flushAndClearCaches(platformCache, categoryCache, genreCache, tagCache);
            log.info("Successfully processed {} games", processed);
        } catch (IOException | CsvValidationException e) {
            log.error("Failed to read file: {}", e.getMessage(), e);
        }
    }

    private VideoGame parseGameLine(String[] line,
                                    Map<String, Platform> platformCache,
                                    Map<String, Category> categoryCache,
                                    Map<String, GameGenre> genreCache,
                                    Map<String, SteamSpyTag> tagCache) {
        VideoGame game = new VideoGame();
        game.setSteamAppId(parseInt(line[0]));
        game.setName(line[1].trim());
        game.setReleaseDate(parseDate(line[2]));
        game.setEnglish("1".equals(line[3].trim()));
        game.setDeveloper(line[4].trim());
        game.setPublisher(line[5].trim());
        game.setRequiredAge(parseInt(line[7]));
        game.setAchievements(parseInt(line[11]));
        game.setPositiveRatings(parseInt(line[12]));
        game.setNegativeRatings(parseInt(line[13]));
        game.setAveragePlaytime(parseInt(line[14]));
        game.setMedianPlaytime(parseInt(line[15]));
        game.setOwners(line[16].trim());
        game.setPrice(parseDouble(line[17]));

        // Обработка связей с кешированием
        processPlatforms(game, line[6], platformCache);
        processCategories(game, line[8], categoryCache);
        processGenres(game, line[9], genreCache);
        processTags(game, line[10], tagCache);

        return game;
    }

    private void processPlatforms(VideoGame game, String rawData, Map<String, Platform> cache) {
        if (rawData == null || rawData.trim().isEmpty()) return;

        Arrays.stream(LIST_SEP.split(rawData))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(name -> {
                    Platform platform = cache.computeIfAbsent(name, key ->
                            platformRepo.findByName(key)
                                    .orElseGet(() -> {
                                        Platform newPlatform = new Platform();
                                        newPlatform.setName(key);
                                        return platformRepo.save(newPlatform);
                                    })
                    );
                    game.getPlatforms().add(platform);
                });
    }

    private void processCategories(VideoGame game, String rawData, Map<String, Category> cache) {
        if (rawData == null || rawData.trim().isEmpty()) return;

        Arrays.stream(LIST_SEP.split(rawData))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(name -> {
                    Category category = cache.computeIfAbsent(name, key ->
                            categoryRepo.findByName(key)
                                    .orElseGet(() -> {
                                        Category newCategory = new Category();
                                        newCategory.setName(key);
                                        return categoryRepo.save(newCategory);
                                    })
                    );
                    game.getCategories().add(category);
                });
    }

    private void processGenres(VideoGame game, String rawData, Map<String, GameGenre> cache) {
        if (rawData == null || rawData.trim().isEmpty()) return;

        Arrays.stream(LIST_SEP.split(rawData))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(name -> {
                    GameGenre genre = cache.computeIfAbsent(name, key ->
                            genreRepo.findByName(key)
                                    .orElseGet(() -> {
                                        GameGenre newGenre = new GameGenre();
                                        newGenre.setName(key);
                                        return genreRepo.save(newGenre);
                                    })
                    );
                    game.getGenres().add(genre);
                });
    }

    private void processTags(VideoGame game, String rawData, Map<String, SteamSpyTag> cache) {
        if (rawData == null || rawData.trim().isEmpty()) return;

        Arrays.stream(LIST_SEP.split(rawData))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(name -> {
                    SteamSpyTag tag = cache.computeIfAbsent(name, key ->
                            tagRepo.findByName(key)
                                    .orElseGet(() -> {
                                        SteamSpyTag newTag = new SteamSpyTag();
                                        newTag.setName(key);
                                        return tagRepo.save(newTag);
                                    })
                    );
                    game.getSteamspyTags().add(tag);
                });
    }

    private void flushAndClearCaches(Map<String, ?>... caches) {
        entityManager.flush();
        entityManager.clear();
        for (Map<String, ?> cache : caches) {
            cache.clear();
        }
    }

    private LocalDate parseDate(String dateStr) {
        try {
            return dateStr == null || dateStr.trim().isEmpty() ?
                    null : LocalDate.parse(dateStr.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Invalid date: {}", dateStr);
            return null;
        }
    }

    private Integer parseInt(String s) {
        if (s == null || s.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid integer: {}", s);
            return 0;
        }
    }

    private Double parseDouble(String s) {
        if (s == null || s.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid double: {}", s);
            return 0.0;
        }
    }
}