package pl.easyoffer.offer_service.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import pl.easyoffer.offer_service.exception.NotFoundException;
import pl.easyoffer.offer_service.mapper.JobOfferMapper;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.JobOfferResponseTO;
import pl.easyoffer.offer_service.model.dto.OfferStatsResponseTO;
import pl.easyoffer.offer_service.model.dto.TopTechnologyTO;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.service.persistence.JobOfferPersistenceService;
import pl.easyoffer.offer_service.service.persistence.TechnologyPersistenceService;
import pl.easyoffer.offer_service.util.OfferSpecificationBuilder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Service
@RequiredArgsConstructor
public class JobOfferService {

    private static final Logger log = LoggerFactory.getLogger(JobOfferService.class);

    private final JobOfferPersistenceService jobOfferPersistenceService;
    private final TechnologyPersistenceService technologyPersistenceService;

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
        normalizeRequest(request);

        Optional<JobOfferEntity> duplicate = findDuplicate(request);
        JobOfferEntity target = duplicate.orElseGet(JobOfferEntity::new);

        if (duplicate.isPresent()) {
            log.info("Duplicate offer detected, updating existing record. offerId={}", target.getId());
        } else {
            log.info("Creating new offer for title='{}', company='{}'", request.getTitle(), request.getCompanyName());
        }

        JobOfferMapper.INSTANCE.updateEntity(request, target);
        target.setTechnologies(resolveTechnologies(request));
        target.setSource(isNotBlank(request.getSource()) ? request.getSource() : JobOfferSourceType.MANUAL.getName());
        JobOfferEntity saved = jobOfferPersistenceService.save(target);
        return JobOfferMapper.INSTANCE.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OfferStatsResponseTO getStats(int topLimit) {
        int normalizedLimit = Math.max(topLimit, 1);
        long totalOffers = jobOfferPersistenceService.count();
        var topTechnologies = jobOfferPersistenceService.findTopTechnologies(PageRequest.of(0, normalizedLimit)).stream()
                .map(row -> new TopTechnologyTO(row.getName(), row.getOfferCount()))
                .toList();
        return new OfferStatsResponseTO(totalOffers, topTechnologies);
    }

    private Optional<JobOfferEntity> findDuplicate(JobOfferRequestTO request) {
        if (StringUtils.hasText(request.getExternalId())) {
            Optional<JobOfferEntity> byExternalId = jobOfferPersistenceService.findByExternalId(request.getExternalId());
            if (byExternalId.isPresent()) {
                return byExternalId;
            }
        }

        return jobOfferPersistenceService.findDuplicateByCoreFields(
                request.getTitle(),
                request.getCompanyName(),
                request.getLocation()
        );
    }

    private Set<TechnologyEntity> resolveTechnologies(JobOfferRequestTO request) {
        if (CollectionUtils.isEmpty(request.getTechnologies())) {
            return Collections.emptySet();
        }

        return request.getTechnologies().stream()
                .map(technologyTO -> findOrCreateTechnology(technologyTO.getName(), technologyTO.getLevel()))
                .collect(Collectors.toSet());
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

    private void normalizeRequest(JobOfferRequestTO request) {
        request.setExternalId(trimToNull(request.getExternalId()));
        request.setTitle(trimToNull(request.getTitle()));
        request.setCompanyName(trimToNull(request.getCompanyName()));
        request.setLocation(trimToNull(request.getLocation()));
        request.setDescription(trimToNull(request.getDescription()));
        request.setExperienceLevel(trimToNull(request.getExperienceLevel()));
        request.setEmploymentType(trimToNull(request.getEmploymentType()));
        request.setWorkMode(trimToNull(request.getWorkMode()));
        request.setCurrency(trimToNull(request.getCurrency()));
        request.setSource(trimToNull(request.getSource()));
        request.setUrl(trimToNull(request.getUrl()));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
