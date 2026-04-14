package pl.easyoffer.offer_service.service.persistence;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.model.domain.Technology;
import pl.easyoffer.offer_service.repository.TechnologyRepository;

@Service
@RequiredArgsConstructor
public class TechnologyPersistenceService {

    private final TechnologyRepository technologyRepository;

    public Optional<Technology> findByName(String name) {
        return technologyRepository.findByName(name);
    }

    public Technology save(Technology technology) {
        return technologyRepository.save(technology);
    }
}
