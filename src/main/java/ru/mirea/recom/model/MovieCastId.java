package ru.mirea.recom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class MovieCastId implements Serializable {
    @Column(name = "movie_id")
    private Integer movieId;

    @Column(name = "person_id")
    private Integer personId;

    @Column(name = "character")
    private String character;

    public MovieCastId(Integer id, Integer id1, String character) {
        this.movieId = id;
        this.personId = id1;
        this.character = character;
    }

    public MovieCastId() {

    }
}
