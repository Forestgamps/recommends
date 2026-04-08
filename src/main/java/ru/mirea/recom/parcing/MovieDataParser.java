package ru.mirea.recom.parcing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.recom.model.Genre;
import ru.mirea.recom.model.Movie;
import ru.mirea.recom.repository.GenreRepository;
import ru.mirea.recom.repository.MovieRepository;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MovieDataParser {
    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void parseAndSaveMovies(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] headers = reader.readNext(); // Пропускаем заголовок

            String[] line;
            while ((line = reader.readNext()) != null) {
                Movie movie = parseMovieLine(line);
                movieRepository.save(movie);
            }
            log.info("Успешно загружены данные о фильмах");
        } catch (IOException | CsvValidationException e) {
            log.error("Ошибка при парсинге файла: {}", e.getMessage());
        }
    }

    private Movie parseMovieLine(String[] line) throws IOException {
        Movie movie = new Movie();
        movie.setId(Integer.parseInt(line[3])); // id
        movie.setTitle(line[17]); // title
        movie.setOriginalTitle(line[6]); // original_title
        movie.setOverview(line[7]); // overview
        movie.setTagline(line[16]); // tagline
        movie.setHomepage(line[2]); // homepage
        movie.setOriginalLanguage(line[5]); // original_language
        movie.setReleaseDate(parseDate(line[11])); // release_date
        movie.setPopularity(line[8].isEmpty() ? 0.0 : Double.parseDouble(line[8])); // popularity
        movie.setBudget(line[0].isEmpty() ? 0L : Long.parseLong(line[0])); // budget
        movie.setRevenue(line[12].isEmpty() ? 0L : Long.parseLong(line[12])); // revenue
        movie.setRuntime(line[13].isEmpty() ? 0 : (int)Double.parseDouble(line[13])); // runtime
        movie.setVoteAverage(line[18].isEmpty() ? 0.0 : Double.parseDouble(line[18])); // vote_average
        movie.setVoteCount(line[19].isEmpty() ? 0 : Integer.parseInt(line[19])); // vote_count
        movie.setStatus(line[15]); // status

        // Парсинг JSON с жанрами
        Set<Genre> genres = parseGenres(line[1]);
        genres.forEach(genre -> {
            genreRepository.save(genre); // Сохраняем жанр, если его нет
            movie.getGenres().add(genre);
        });

        return movie;
    }

    private Set<Genre> parseGenres(String json) throws IOException {
        return objectMapper.readValue(json, new TypeReference<Set<Genre>>() {});
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        return LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
    }
}
