package ru.mirea.recom.parcing;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.recom.model.games.VideoGameMedia;
import ru.mirea.recom.repository.VideoGameMediaRepository;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VideoGameMediaDataParser {
    private final VideoGameMediaRepository mediaRepository;

    // Включаем поддержку одинарных кавычек и нестрогих имён полей
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);

    public void parseAndSaveMedia(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            // Пропускаем заголовок
            String[] header = reader.readNext();
            if (header != null && header[0].startsWith("\uFEFF")) {
                header[0] = header[0].substring(1);
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                // Ожидаем 5 колонок: appid, header_image, screenshots, background, movies
                if (line.length < 5) {
                    log.warn("Media CSV: слишком мало колонок ({}), пропускаем строку: {}", line.length, Arrays.toString(line));
                    continue;
                }

                try {
                    VideoGameMedia media = parseMediaLine(line);
                    mediaRepository.save(media);
                    log.info("Saved VideoGameMedia for appId {}", media.getSteamAppId());
                } catch (Exception ex) {
                    log.error("Не удалось распарсить/сохранить media для appId {}: {}", line[0], ex.getMessage());
                }
            }

            log.info("Импорт медиа видеоигр завершён");
        } catch (IOException | CsvValidationException e) {
            log.error("Ошибка при чтении media CSV {}: {}", filePath, e.getMessage(), e);
        }
    }

    private VideoGameMedia parseMediaLine(String[] line) throws IOException {
        VideoGameMedia media = new VideoGameMedia();
        media.setSteamAppId(Integer.parseInt(line[0].trim()));
        media.setHeaderImage(line[1].trim());

        // Скриншоты: убираем одинарные кавычки → валидный JSON
        String rawScreens = line[2].trim()
                .replace("'", "\"")
                // иногда в CSV двойные кавычки удваиваются
                .replace("\"\"", "\"");
        List<Map<String, Object>> screenshots = objectMapper.readValue(
                rawScreens,
                new TypeReference<>() {}
        );
        // сохраняем «чистый» JSON
        media.setScreenshotsJson(objectMapper.writeValueAsString(screenshots));

        // background может быть пустым
        media.setBackground(line[3].trim().isEmpty() ? null : line[3].trim());

        // Аналогично для movies (трейлеры/ролики)
        String rawMovies = line[4].trim();
        if (!rawMovies.isEmpty()) {
            // если формат аналогичен — можно тоже replace("'", "\"")
            media.setMovies(rawMovies);
        }

        return media;
    }
}
