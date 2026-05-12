package pl.easyoffer.offer_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import pl.easyoffer.offer_service.exception.NotFoundException;
import pl.easyoffer.offer_service.mapper.JobOfferMapper;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.JobOfferResponseTO;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.service.deduplication.JobOfferDuplicateFinder;
import pl.easyoffer.offer_service.service.persistence.JobOfferPersistenceService;
import pl.easyoffer.offer_service.service.persistence.TechnologyPersistenceService;
import pl.easyoffer.offer_service.util.OfferSpecificationBuilder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobOfferService {

    private final JobOfferPersistenceService jobOfferPersistenceService;
    private final TechnologyPersistenceService technologyPersistenceService;
    private final JobOfferDuplicateFinder jobOfferDuplicateFinder;

    @Transactional(readOnly = true)
    public Page<JobOfferResponseTO> getOffers(Pageable pageable) {
        return jobOfferPersistenceService.findAll(pageable).map(JobOfferMapper.INSTANCE::toResponse);
    }

    @Transactional(readOnly = true)
    public JobOfferResponseTO getById(Long id) {
        JobOfferEntity offer = jobOfferPersistenceService.findById(id)
                .orElseThrow(() -> new NotFoundException("Offer not found for id: " + id));
        return JobOfferMapper.INSTANCE.toResponse(offer);
    }

    @Transactional(readOnly = true)
    public Page<JobOfferResponseTO> search(String technology, String location, String experienceLevel, Pageable pageable) {
        return jobOfferPersistenceService.search(OfferSpecificationBuilder.build(technology, location, experienceLevel), pageable)
                .map(JobOfferMapper.INSTANCE::toResponse);
    }

    @Transactional
    public JobOfferResponseTO createOrUpdate(JobOfferRequestTO request) {
        JobOfferMapper.INSTANCE.normalizeRequest(request);
        Optional<JobOfferEntity> duplicatedOffer = jobOfferDuplicateFinder.find(request);
        JobOfferEntity entity = duplicatedOffer.orElseGet(JobOfferEntity::new);
        JobOfferMapper.INSTANCE.updateEntity(request, entity);
        entity.setTechnologies(resolveTechnologies(request));
        JobOfferEntity savedEntity = jobOfferPersistenceService.save(entity);
        return JobOfferMapper.INSTANCE.toResponse(savedEntity);
    }

    private Set<TechnologyEntity> resolveTechnologies(JobOfferRequestTO request) {
        return Optional.of(request)
                .map(JobOfferRequestTO::getTechnologies)
                .filter(technologies -> !CollectionUtils.isEmpty(technologies))
                .map(technologies -> technologies.stream()
                        .map(technologyTO -> findOrCreateTechnology(technologyTO.getName(), technologyTO.getLevel()))
                        .collect(Collectors.toSet())
                )
                .orElse(Collections.emptySet());
    }

    private TechnologyEntity findOrCreateTechnology(String name, Integer level) {
        return technologyPersistenceService.findByNameAndLevel(name, level)
                .orElseGet(() -> {
                    TechnologyEntity technologyEntity = new TechnologyEntity();
                    technologyEntity.setName(name);
                    technologyEntity.setLevel(level);
                    return technologyPersistenceService.save(technologyEntity);
                });
    }

}
