package ru.mirea.recom.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movie_cast")
@Getter
@Setter
public class MovieCast {
    @EmbeddedId
    private MovieCastId id;

    @ManyToOne
    @MapsId("movieId")
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @MapsId("personId")
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "\"order\"")
    private Integer order;

    // Удаляем @Column(name = "character"), так как оно уже есть в MovieCastId
    public String getCharacter() {
        return id.getCharacter();
    }

    public void setCharacter(String character) {
        if (id == null) {
            id = new MovieCastId();
        }
        id.setCharacter(character);
    }
}
