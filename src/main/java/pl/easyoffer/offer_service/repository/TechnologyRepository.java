package pl.easyoffer.offer_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.easyoffer.offer_service.model.domain.Technology;

public interface TechnologyRepository extends JpaRepository<Technology, Long> {
    Optional<Technology> findByName(String name);
}
