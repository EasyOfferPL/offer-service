package pl.easyoffer.offer_service.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;

public interface TechnologyRepository extends JpaRepository<TechnologyEntity, Long> {
    Optional<TechnologyEntity> findByNameAndLevel(String name, Integer level);
}
