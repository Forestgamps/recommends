// src/main/java/ru/mirea/recom/repository/SteamSpyTagRepository.java
package ru.mirea.recom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.recom.model.games.SteamSpyTag;

import java.util.Optional;

public interface SteamSpyTagRepository extends JpaRepository<SteamSpyTag, Long> {
    Optional<SteamSpyTag> findByName(String name);
}
