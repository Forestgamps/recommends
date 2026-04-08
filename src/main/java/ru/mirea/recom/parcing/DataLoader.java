package ru.mirea.recom.parcing;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final MovieDataParser movieDataParser;
    private final CreditsDataParser creditsDataParser;

    private final VideoGameDataParser videoGameDataParser;
    private final VideoGameMediaDataParser videoGameMediaDataParser;
    private final SteamSpyTagDataParser steamSpyTagDataParser;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String moviesPath = "src/main/resources/data/tmdb_5000_movies.csv";
        String creditsPath = "src/main/resources/data/tmdb_5000_credits.csv";



        String steamPath = "src/main/resources/data/steam.csv";
        String steamMedia = "src/main/resources/data/steam_media_data.csv";
        String steamSpy = "src/main/resources/data/steamspy_tag_data.csv";
        String steamDesc = "src/main/resources/data/steam_description_data.csv";
        //movieDataParser.parseAndSaveMovies(moviesPath);
        //creditsDataParser.parseAndSaveCredits(creditsPath);

//        try {
//            // 1. Сначала загружаем медиа (если они не зависят от игр)
//            videoGameMediaDataParser.parseAndSaveMedia(steamMedia);
//
//            // 2. Затем загружаем теги (если они не зависят от игр)
//            steamSpyTagDataParser.parseAndSaveTags(steamSpy);
//
//            // 3. Основные данные игр (последними, так как могут ссылаться на медиа/теги)
//            videoGameDataParser.parseAndSaveVideoGames(steamPath);
//
//            //log.info("All datasets loaded successfully");
//        } catch (Exception e) {
//            //log.error("Data loading failed", e);
//            throw new RuntimeException("Data loading failed", e);
//        }

    }
}