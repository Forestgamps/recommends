package ru.mirea.recom.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mirea.recom.model.games.VideoGame;

import java.util.List;
import java.util.Optional;

public interface VideoGameRepository extends JpaRepository<VideoGame, Integer> {
    // Метод для получения игр, отсортированных по положительным оценкам
    List<VideoGame> findAllByOrderByPositiveRatingsDesc();

    Optional<VideoGame> findBySteamAppId(Integer steamAppId);

    // Альтернативный вариант с использованием @Query
    @Query("SELECT g FROM VideoGame g ORDER BY g.positiveRatings DESC")
    List<VideoGame> findTopRatedGames();

    // Метод для получения популярных игр (по соотношению положительных/отрицательных оценок)
    @Query("SELECT g FROM VideoGame g ORDER BY (g.positiveRatings * 1.0 / (g.positiveRatings + g.negativeRatings)) DESC")
    List<VideoGame> findPopularGames(PageRequest of);

    List<VideoGame> findByNameContainingIgnoreCase(String name);

    @Query("SELECT DISTINCT g FROM VideoGame g " +
            "LEFT JOIN FETCH g.genres " +
            "LEFT JOIN FETCH g.platforms " +
            "WHERE g.steamAppId IN :ids")
    List<VideoGame> findAllWithDetailsByIds(@Param("ids") List<Integer> ids);
}
