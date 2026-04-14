package pl.easyoffer.offer_service.service.persistence;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.easyoffer.offer_service.model.domain.JobOffer;
import pl.easyoffer.offer_service.repository.JobOfferRepository;
import pl.easyoffer.offer_service.repository.projection.TechnologyCountProjection;

@Service
@RequiredArgsConstructor
public class JobOfferPersistenceService {

    private final JobOfferRepository jobOfferRepository;

    public Page<JobOffer> findAll(Pageable pageable) {
        return jobOfferRepository.findAll(pageable);
    }

    public Optional<JobOffer> findById(Long id) {
        return jobOfferRepository.findById(id);
    }

    public Page<JobOffer> search(Specification<JobOffer> specification, Pageable pageable) {
        return jobOfferRepository.findAll(specification, pageable);
    }

    public JobOffer save(JobOffer offer) {
        return jobOfferRepository.save(offer);
    }

    public Optional<JobOffer> findByExternalId(String externalId) {
        return jobOfferRepository.findByExternalId(externalId);
    }

    public Optional<JobOffer> findDuplicateByCoreFields(String title, String companyName, String location) {
        return jobOfferRepository.findDuplicateByCoreFields(title, companyName, location);
    }

    public long count() {
        return jobOfferRepository.count();
    }

    public java.util.List<TechnologyCountProjection> findTopTechnologies(Pageable pageable) {
        return jobOfferRepository.findTopTechnologies(pageable);
    }
}
