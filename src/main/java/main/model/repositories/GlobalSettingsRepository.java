package main.model.repositories;

import main.model.entities.GlobalSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalSettingsRepository extends JpaRepository<GlobalSettings, Integer> {
}