// src/main/java/ru/mirea/recom/model/games/Platform.java
package ru.mirea.recom.model.games;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "platforms")
public class Platform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

//    @ManyToMany(mappedBy = "platforms")
    @ManyToMany()
    private Set<VideoGame> games = new HashSet<>();
}
