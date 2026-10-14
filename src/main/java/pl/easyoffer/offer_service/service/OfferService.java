package pl.easyoffer.offer_service.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import pl.easyoffer.offer_service.exception.NotFoundException;
import pl.easyoffer.offer_service.mapper.OfferMapper;
import pl.easyoffer.offer_service.model.OfferSearchRequest;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.OfferResponseTO;
import pl.easyoffer.offer_service.service.deduplication.CrossSourceFingerprintV1;
import pl.easyoffer.offer_service.service.deduplication.OfferDuplicateFinder;
import pl.easyoffer.offer_service.service.normalization.TechnologyNormalizationResult;
import pl.easyoffer.offer_service.service.normalization.TechnologyNormalizer;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceOutcome;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceService;
import pl.easyoffer.offer_service.service.persistence.TechnologyPersistenceService;
import pl.easyoffer.offer_service.util.OfferSpecificationBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferPersistenceService offerPersistenceService;
    private final TechnologyPersistenceService technologyPersistenceService;
    private final OfferDuplicateFinder offerDuplicateFinder;
    private final TechnologyNormalizer technologyNormalizer;

    @Transactional(readOnly = true)
    public List<OfferResponseTO> search(OfferSearchRequest offerSearchRequest) {
        var specification = builSpecification(offerSearchRequest);
        return offerPersistenceService.search(specification).stream()
                .map(OfferMapper.INSTANCE::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<OfferResponseTO> search(OfferSearchRequest offerSearchRequest, @Nullable Pageable pageable) {
        var specification = builSpecification(offerSearchRequest);
        return offerPersistenceService.search(specification, pageable)
                .map(OfferMapper.INSTANCE::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<OfferResponseTO> getOffers(Pageable pageable) {
        return offerPersistenceService.findAll(pageable).map(OfferMapper.INSTANCE::toResponse);
    }

    @Transactional(readOnly = true)
    public OfferResponseTO getById(Long id) {
        OfferEntity offer = offerPersistenceService.findById(id)
                .orElseThrow(() -> new NotFoundException("Offer not found for id: " + id));
        return OfferMapper.INSTANCE.toResponse(offer);
    }

    @Transactional
    public OfferResponseTO createOrUpdate(OfferRequestTO request) {
        return persist(request).response();
    }

    @Transactional
    public OfferPersistenceOutcome createOrUpdateForSynchronization(OfferRequestTO request) {
        return persist(request).outcome();
    }

    private OfferPersistenceResult persist(OfferRequestTO request) {
        OfferMapper.INSTANCE.normalizeRequest(request);
        Optional<OfferEntity> duplicatedOffer = offerDuplicateFinder.find(request);
        OfferEntity entity = duplicatedOffer.orElseGet(OfferEntity::new);
        OfferMapper.INSTANCE.updateEntity(request, entity);
        entity.setDeduplicationFingerprint(CrossSourceFingerprintV1.calculate(
                request.getTitle(), request.getCompanyName(), request.getLocation()
        ).orElse(null));
        entity.setTechnologies(resolveTechnologies(request));
        OfferEntity savedEntity = offerPersistenceService.save(entity);
        OfferPersistenceOutcome outcome = duplicatedOffer.isPresent()
                ? OfferPersistenceOutcome.UPDATED
                : OfferPersistenceOutcome.CREATED;
        return new OfferPersistenceResult(OfferMapper.INSTANCE.toResponse(savedEntity), outcome);
    }

    private Set<TechnologyEntity> resolveTechnologies(OfferRequestTO request) {
        return Optional.of(request)
                .map(OfferRequestTO::getTechnologies)
                .filter(technologies -> !CollectionUtils.isEmpty(technologies))
                .map(technologies -> technologies.stream()
                        .flatMap(technologyTO -> technologyNormalizer.normalize(technologyTO.getName())
                                .stream()
                                .map(normalizedTechnology -> findOrCreateTechnology(
                                        normalizedTechnology, technologyTO.getLevel()
                                )))
                        .collect(Collectors.toSet())
                )
                .orElse(Collections.emptySet());
    }

    private TechnologyEntity findOrCreateTechnology(
            TechnologyNormalizationResult normalization,
            Integer level
    ) {
        return technologyPersistenceService.findByNameAndLevel(normalization.sourceName(), level)
                .map(technology -> updateNormalization(technology, normalization))
                .orElseGet(() -> createTechnology(normalization, level));
    }

    private TechnologyEntity updateNormalization(
            TechnologyEntity technology,
            TechnologyNormalizationResult normalization
    ) {
        if (Objects.equals(technology.getCanonicalName(), normalization.canonicalName())
                && Objects.equals(technology.getNormalizationVersion(), normalization.version())) {
            return technology;
        }
        technology.setCanonicalName(normalization.canonicalName());
        technology.setNormalizationVersion(normalization.version());
        return technologyPersistenceService.save(technology);
    }

    private TechnologyEntity createTechnology(
            TechnologyNormalizationResult normalization,
            Integer level
    ) {
        TechnologyEntity technology = new TechnologyEntity();
        technology.setName(normalization.sourceName());
        technology.setCanonicalName(normalization.canonicalName());
        technology.setNormalizationVersion(normalization.version());
        technology.setLevel(level);
        return technologyPersistenceService.save(technology);
    }

    private Specification<OfferEntity> builSpecification(OfferSearchRequest offerSearchRequest) {
        return OfferSpecificationBuilder.builder()
                .withIds(offerSearchRequest.getIds())
                .withExternalIds(offerSearchRequest.getExternalIds())
                .withTitles(offerSearchRequest.getTitles())
                .withCompanyNames(offerSearchRequest.getCompanyNames())
                .withLocations(offerSearchRequest.getLocations())
                .withCategoryNames(offerSearchRequest.getCategoryNames())
                .withExperienceLevels(offerSearchRequest.getExperienceLevels())
                .withEmploymentTypes(offerSearchRequest.getEmploymentTypes())
                .withWorkModes(offerSearchRequest.getWorkModes())
                .withSalaryBetween(offerSearchRequest.getSalaryFrom(), offerSearchRequest.getSalaryTo())
                .withSalaryUnits(offerSearchRequest.getSalaryUnits())
                .withCurrencies(offerSearchRequest.getCurrencies())
                .withSources(offerSearchRequest.getSources())
                .withUrls(offerSearchRequest.getUrls())
                .withLanguages(offerSearchRequest.getLanguages())
                .withCreatedAtBetween(offerSearchRequest.getCreatedAtFrom(), offerSearchRequest.getCreatedAtTo())
                .withUpdatedAtBetween(offerSearchRequest.getUpdatedAtFrom(), offerSearchRequest.getUpdatedAtTo())
                .withTechnologies(technologyNormalizer.normalizeCanonicalNames(
                        offerSearchRequest.getTechnologies()
                ))
                .withPublishedAtBetween(offerSearchRequest.getPublishedAtFrom(), offerSearchRequest.getPublishedAtTo())
                .build();
    }

    private record OfferPersistenceResult(
            OfferResponseTO response,
            OfferPersistenceOutcome outcome
    ) {
    }

}
