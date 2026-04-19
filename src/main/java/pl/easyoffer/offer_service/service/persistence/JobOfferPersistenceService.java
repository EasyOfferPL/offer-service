package pl.easyoffer.offer_service.service.persistence;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;
import pl.easyoffer.offer_service.repository.JobOfferRepository;
import pl.easyoffer.offer_service.repository.projection.TechnologyCountProjection;

@Service
@RequiredArgsConstructor
public class JobOfferPersistenceService {

    private final JobOfferRepository jobOfferRepository;

    public Page<JobOfferEntity> findAll(Pageable pageable) {
        return jobOfferRepository.findAll(pageable);
    }

    public Optional<JobOfferEntity> findById(Long id) {
        return jobOfferRepository.findById(id);
    }

    public Page<JobOfferEntity> search(Specification<JobOfferEntity> specification, Pageable pageable) {
        return jobOfferRepository.findAll(specification, pageable);
    }

    public JobOfferEntity save(JobOfferEntity offer) {
        return jobOfferRepository.save(offer);
    }

    public Optional<JobOfferEntity> findByExternalId(String externalId) {
        return jobOfferRepository.findByExternalId(externalId);
    }

    public Optional<JobOfferEntity> findDuplicateByCoreFields(String title, String companyName, String location) {
        return jobOfferRepository.findDuplicateByCoreFields(title, companyName, location);
    }

    public long count() {
        return jobOfferRepository.count();
    }

    public java.util.List<TechnologyCountProjection> findTopTechnologies(Pageable pageable) {
        return jobOfferRepository.findTopTechnologies(pageable);
    }
}
