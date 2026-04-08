package ru.mirea.recom.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "movie_crew")
@Getter
@Setter
public class MovieCrew {
    @EmbeddedId
    private MovieCrewId id;

    @ManyToOne
    @MapsId("movieId")
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne
    @MapsId("personId")
    @JoinColumn(name = "person_id")
    private Person person;

    @Column(name = "department")
    private String department;

    // Удаляем @Column(name = "character"), так как оно уже есть в MovieCastId
    public String getJob() {
        return id.getJob();
    }

    public void setJob(String character) {
        if (id == null) {
            id = new MovieCrewId();
        }
        id.setJob(character);
    }
}
