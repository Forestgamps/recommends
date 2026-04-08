package ru.mirea.recom.parcing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.recom.model.*;
import ru.mirea.recom.repository.MovieRepository;
import ru.mirea.recom.repository.PersonRepository;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditsDataParser {

    private static final int BATCH_SIZE = 1_000;
    private static final int CHARACTER_MAX = 255; // DB column limit

    private final MovieRepository movieRepository;
    private final PersonRepository personRepository;
    private final EntityManager entityManager;

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Transactional
    public void parseAndSaveCredits(String filePath) {
        Map<Integer, Person> personCache = new HashMap<>();
        int processed = 0;
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            reader.readNext(); // header
            String[] line;
            while ((line = reader.readNext()) != null) {
                processed++;
                Integer movieId = Integer.parseInt(line[0]);
                Movie movie = movieRepository.findById(movieId).orElse(null);
                if (movie == null) continue;

                try {
                    if (hasText(line[2]))
                        parseCast(normalizeJson(line[2]), movie, personCache);
                    if (hasText(line[3]))
                        parseCrew(normalizeJson(line[3]), movie, personCache);
                } catch (JsonProcessingException ex) {
                    log.warn("JSON error in movieId {}: {}", movieId, ex.getOriginalMessage());
                    continue;
                }

                movieRepository.save(movie);
                if (processed % BATCH_SIZE == 0) {
                    flushAndClear(personCache);
                    log.info("Processed {} rows", processed);
                }
            }
            flushAndClear(personCache);
            log.info("Import finished. Total rows: {}", processed);
        } catch (IOException | CsvValidationException e) {
            log.error("File read error: {}", e.getMessage(), e);
        }
    }

    /* ===== JSON helpers ===== */

    private void parseCast(String json, Movie movie, Map<Integer, Person> cache) throws JsonProcessingException {
        List<CastMember> members = mapper.readValue(json, new TypeReference<List<CastMember>>() {});
        for (CastMember m : members) {
            Person person = resolvePerson(m.getId(), m.getName(), m.getGender(), cache);
            MovieCast mc = new MovieCast();
            String character = safeCharacter(m.getCharacter());
            mc.setId(new MovieCastId(movie.getId(), person.getId(), character));
            mc.setMovie(movie);
            mc.setPerson(person);
            mc.setCharacter(character);
            mc.setOrder(m.getOrder());
            movie.getCast().add(mc);
        }
    }

    private void parseCrew(String json, Movie movie, Map<Integer, Person> cache) throws JsonProcessingException {
        List<CrewMember> members = mapper.readValue(json, new TypeReference<List<CrewMember>>() {});
        for (CrewMember m : members) {
            if (!("Directing".equals(m.getDepartment()) && "Director".equals(m.getJob()))) continue;
            Person person = resolvePerson(m.getId(), m.getName(), m.getGender(), cache);
            MovieCrew mc = new MovieCrew();
            mc.setId(new MovieCrewId(movie.getId(), person.getId(), m.getJob()));
            mc.setMovie(movie);
            mc.setPerson(person);
            mc.setDepartment(m.getDepartment());
            mc.setJob(m.getJob());
            movie.getCrew().add(mc);
        }
    }

    /* ===== Utility ===== */

    private String normalizeJson(String raw) {
        String s = raw.trim();
        if (s.length() > 1 && s.charAt(0) == '"' && (s.charAt(1) == '[' || s.charAt(1) == '{')) {
            int last = s.length() - 1;
            if (s.charAt(last) == '"' && (s.charAt(last - 1) == ']' || s.charAt(last - 1) == '}')) {
                s = s.substring(1, last);
            }
        }
        return s.replace("\"\"", "\"");
    }

    private String safeCharacter(String character) {
        if (character == null) return null;
        return character.length() <= CHARACTER_MAX ? character : character.substring(0, CHARACTER_MAX);
    }

    private Person resolvePerson(Integer id, String name, Integer gender, Map<Integer, Person> cache) {
        return cache.computeIfAbsent(id, key -> {
            Person p = personRepository.findById(key).orElseGet(Person::new);
            p.setId(key);
            p.setName(name);
            p.setGender(gender);
            return personRepository.save(p);
        });
    }

    private void flushAndClear(Map<Integer, Person> cache) {
        entityManager.flush();
        entityManager.clear();
        cache.clear();
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty() && !"\\N".equals(s.trim());
    }

    /* ===== DTO ===== */

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CastMember {
        @JsonProperty("cast_id")
        private Integer castId;
        @JsonProperty("credit_id")
        private String creditId;
        private String character;
        private Integer gender;
        private Integer id;
        private String name;
        private Integer order;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CrewMember {
        private Integer id;
        private String name;
        private Integer gender;
        private String department;
        private String job;
    }
}
