package ru.mirea.recom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.recom.model.games.VideoGameMedia;

import java.util.Optional;

public interface VideoGameMediaRepository extends JpaRepository<VideoGameMedia, Integer> {
    Optional<VideoGameMedia> findBySteamAppId(Integer steamAppId);
}
