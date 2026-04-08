package ru.mirea.recom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.recom.model.games.SteamSpyTagData;

public interface SteamSpyTagDataRepository extends JpaRepository<SteamSpyTagData, Integer> {
}
