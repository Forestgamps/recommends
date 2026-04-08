package ru.mirea.recom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.recom.model.games.VideoGame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameVectorizer {
    // Веса для разных признаков (можно настраивать)
    private static final double GENRE_WEIGHT = 0.4;
    private static final double TAG_WEIGHT = 0.3;
    private static final double DEVELOPER_WEIGHT = 0.2;
    private static final double DESCRIPTION_WEIGHT = 0.1;

    public Map<String, Double> vectorize(VideoGame game) {
        Map<String, Double> vector = new HashMap<>();

        // 1. Жанры
        game.getGenres().forEach(genre ->
                vector.merge("genre_" + genre.getName(), GENRE_WEIGHT, Double::sum));

        // 2. Теги (топ-10 по популярности)
        game.getSteamspyTags().stream()
                .limit(10)
                .forEach(tag ->
                        vector.merge("tag_" + tag.getName(), TAG_WEIGHT, Double::sum));

        // 3. Разработчики
        if (game.getDeveloper() != null) {
            Arrays.stream(game.getDeveloper().split(";"))
                    .forEach(dev ->
                            vector.merge("developer_" + dev.trim(), DEVELOPER_WEIGHT, Double::sum));
        }

        // 4. Ключевые слова из описания (если есть)
//        if (game.getDescription() != null) {
//            Arrays.stream(game.getDescription().split("\\s+"))
//                    .filter(word -> word.length() > 3)
//                    .limit(20)
//                    .forEach(word ->
//                            vector.merge("word_" + word.toLowerCase(), DESCRIPTION_WEIGHT, Double::sum));
//        }

        return vector;
    }
}