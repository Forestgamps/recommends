package ru.mirea.recom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.recom.model.games.Platform;

import java.util.Optional;

public interface PlatformRepository extends JpaRepository<Platform, Long> {
    Optional<Platform> findByName(String name);
}
