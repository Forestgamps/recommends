package ru.mirea.recom.model.games;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import ru.mirea.recom.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class RecommendationHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String recommendationType; // "similar-games", "user-games", "popular-games", "similar-movies", etc.
    
    @ElementCollection
    private List<Integer> contentIds; // ID рекомендованного контента
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}