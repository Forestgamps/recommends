package ru.mirea.recom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.mirea.recom.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
