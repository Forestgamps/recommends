package ru.mirea.recom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.recom.model.Person;

public interface PersonRepository extends JpaRepository<Person, Integer> {
}
