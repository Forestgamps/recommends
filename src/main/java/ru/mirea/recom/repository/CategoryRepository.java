// src/main/java/ru/mirea/recom/repository/CategoryRepository.java
package ru.mirea.recom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.recom.model.games.Category;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
}
