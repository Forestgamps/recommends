// src/main/java/ru/mirea/recom/repository/GameGenreRepository.java
package ru.mirea.recom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.recom.model.games.GameGenre;

import java.util.Optional;

public interface GameGenreRepository extends JpaRepository<GameGenre, Long> {
    Optional<GameGenre> findByName(String name);
}
