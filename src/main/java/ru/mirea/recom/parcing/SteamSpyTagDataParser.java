package ru.mirea.recom.parcing;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.recom.model.games.SteamSpyTagData;
import ru.mirea.recom.repository.SteamSpyTagDataRepository;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SteamSpyTagDataParser {
    private final SteamSpyTagDataRepository tagRepository;

    public void parseAndSaveTags(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] header = reader.readNext();
            if (header == null || header.length == 0) {
                log.error("Пустой заголовок файла тегов");
                return;
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                SteamSpyTagData tagData = parseTagLine(header, line);
                tagRepository.save(tagData);
            }
            log.info("Успешно загружены данные о тегах видеоигр");
        } catch (IOException | CsvValidationException e) {
            log.error("Ошибка при парсинге файла тегов: {}", e.getMessage());
        }
    }

    private SteamSpyTagData parseTagLine(String[] header, String[] line) {
        SteamSpyTagData tagData = new SteamSpyTagData();
        // Первая колонка – appid
        tagData.setAppId(Integer.parseInt(line[0]));
        Map<String, Integer> tags = new HashMap<>();
        for (int i = 1; i < header.length && i < line.length; i++) {
            try {
                tags.put(header[i], Integer.parseInt(line[i]));
            } catch (NumberFormatException e) {
                tags.put(header[i], 0);
            }
        }
        tagData.setTags(tags);
        return tagData;
    }
}
