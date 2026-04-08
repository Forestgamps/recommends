package ru.mirea.recom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@EqualsAndHashCode
public class MovieCrewId {
    @Column(name = "movie_id")
    private Integer movieId;

    @Column(name = "person_id")
    private Integer personId;

    @Column(name = "job")
    private String job;

    public MovieCrewId(Integer id, Integer id1, String job) {
        this.movieId = id;
        this.personId = id1;
        this.job = job;
    }

    public MovieCrewId() {

    }
}
