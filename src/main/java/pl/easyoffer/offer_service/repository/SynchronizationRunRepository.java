package pl.easyoffer.offer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.easyoffer.offer_service.model.entity.SynchronizationRunEntity;

public interface SynchronizationRunRepository extends JpaRepository<SynchronizationRunEntity, Long> {
}
