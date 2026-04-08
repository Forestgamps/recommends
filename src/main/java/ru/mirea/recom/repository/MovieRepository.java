package ru.mirea.recom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.mirea.recom.model.Movie;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.genres WHERE m.id IN :ids")
    List<Movie> findAllWithGenresByIds(@Param("ids") List<Integer> ids);
}
