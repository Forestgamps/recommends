package ru.mirea.recom.repository;

import org.springframework.beans.PropertyValues;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.recom.model.User;
import ru.mirea.recom.model.games.RecommendationHistory;

import java.util.List;

public interface RecommendationHistoryRepository extends JpaRepository<RecommendationHistory, Long> {
    List<RecommendationHistory> findByUserOrderByCreatedAtDesc(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM RecommendationHistory h WHERE h.user = :user")
    int deleteByUser(@Param("user") User user);

    List<RecommendationHistory> findByUserUsernameOrderByCreatedAtDesc(String username);

    List<RecommendationHistory> findByUserAndRecommendationType(User user, String s);
}