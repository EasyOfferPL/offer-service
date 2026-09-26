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
import pl.easyoffer.offer_service.service.deduplication.OfferDuplicateFinder;
import pl.easyoffer.offer_service.service.persistence.OfferPersistenceService;
import pl.easyoffer.offer_service.service.persistence.TechnologyPersistenceService;
import pl.easyoffer.offer_service.util.OfferSpecificationBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferPersistenceService offerPersistenceService;
    private final TechnologyPersistenceService technologyPersistenceService;
    private final OfferDuplicateFinder offerDuplicateFinder;

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
        OfferMapper.INSTANCE.normalizeRequest(request);
        Optional<OfferEntity> duplicatedOffer = offerDuplicateFinder.find(request);
        OfferEntity entity = duplicatedOffer.orElseGet(OfferEntity::new);
        OfferMapper.INSTANCE.updateEntity(request, entity);
        entity.setTechnologies(resolveTechnologies(request));
        OfferEntity savedEntity = offerPersistenceService.save(entity);
        return OfferMapper.INSTANCE.toResponse(savedEntity);
    }

    private Set<TechnologyEntity> resolveTechnologies(OfferRequestTO request) {
        return Optional.of(request)
                .map(OfferRequestTO::getTechnologies)
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

    private static Specification<OfferEntity> builSpecification(OfferSearchRequest offerSearchRequest) {
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
                .withTechnologies(offerSearchRequest.getTechnologies())
                .build();
    }

}
