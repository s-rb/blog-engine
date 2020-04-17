package main.model.repositories;

import main.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query(value = "SELECT * FROM users u" +
            " WHERE u.email = ?", nativeQuery = true)
    User getUserByEmail(String email);

    @Query(value = "SELECT * FROM users u" +
            " WHERE u.code = ?", nativeQuery = true)
    User getUserByCode(String code);

    @Query(value = "SELECT * FROM users u" +
            " WHERE u.name = ?", nativeQuery = true)
    User getUserByName(String name);
}