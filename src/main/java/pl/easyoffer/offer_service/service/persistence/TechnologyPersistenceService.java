package pl.easyoffer.offer_service.service.persistence;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.repository.TechnologyRepository;

@Service
@RequiredArgsConstructor
public class TechnologyPersistenceService {

    private final TechnologyRepository technologyRepository;

    public Optional<TechnologyEntity> findByNameAndLevel(String name, Integer level) {
        return technologyRepository.findByNameAndLevel(name, level);
    }

    public TechnologyEntity save(TechnologyEntity technologyEntity) {
        return technologyRepository.save(technologyEntity);
    }
}
