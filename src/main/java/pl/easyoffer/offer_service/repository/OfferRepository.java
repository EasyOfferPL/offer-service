package pl.easyoffer.offer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.easyoffer.offer_service.model.OfferEntity;

@Repository
public interface OfferRepository extends JpaRepository<OfferEntity,Long> {
}
