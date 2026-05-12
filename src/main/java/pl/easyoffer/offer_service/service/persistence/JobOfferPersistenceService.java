package pl.easyoffer.offer_service.service.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;
import pl.easyoffer.offer_service.repository.JobOfferRepository;

import java.util.Optional;

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

    public Optional<JobOfferEntity> findByCoreFields(String title, String companyName, String location) {
        return jobOfferRepository.findByCoreFields(title, companyName, location);
    }

}
