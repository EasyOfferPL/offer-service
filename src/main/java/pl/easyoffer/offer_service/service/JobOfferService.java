package pl.easyoffer.offer_service.service;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import pl.easyoffer.offer_service.model.domain.JobOffer;
import pl.easyoffer.offer_service.model.domain.Technology;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.JobOfferResponseTO;
import pl.easyoffer.offer_service.model.dto.OfferStatsResponseTO;
import pl.easyoffer.offer_service.model.dto.TopTechnologyTO;
import pl.easyoffer.offer_service.exception.NotFoundException;
import pl.easyoffer.offer_service.exception.ValidationException;
import pl.easyoffer.offer_service.mapper.JobOfferMapper;
import pl.easyoffer.offer_service.service.persistence.JobOfferPersistenceService;
import pl.easyoffer.offer_service.service.persistence.TechnologyPersistenceService;
import pl.easyoffer.offer_service.util.OfferSpecificationBuilder;

@Service
@RequiredArgsConstructor
public class JobOfferService {

    private static final Logger log = LoggerFactory.getLogger(JobOfferService.class);

    private final JobOfferPersistenceService jobOfferPersistenceService;
    private final TechnologyPersistenceService technologyPersistenceService;
    private final JobOfferMapper jobOfferMapper;

    @Transactional(readOnly = true)
    public Page<JobOfferResponseTO> getOffers(Pageable pageable) {
        return jobOfferPersistenceService.findAll(pageable).map(jobOfferMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public JobOfferResponseTO getById(Long id) {
        JobOffer offer = jobOfferPersistenceService.findById(id)
                .orElseThrow(() -> new NotFoundException("Offer not found for id: " + id));
        return jobOfferMapper.toResponse(offer);
    }

    @Transactional(readOnly = true)
    public Page<JobOfferResponseTO> search(String technology, String location, String experienceLevel, Pageable pageable) {
        return jobOfferPersistenceService.search(OfferSpecificationBuilder.build(technology, location, experienceLevel), pageable)
                .map(jobOfferMapper::toResponse);
    }

    @Transactional
    public JobOfferResponseTO createOrUpdate(JobOfferRequestTO request) {
        validateRequest(request);
        normalizeRequest(request);

        Optional<JobOffer> duplicate = findDuplicate(request);
        JobOffer target = duplicate.orElseGet(JobOffer::new);

        if (duplicate.isPresent()) {
            log.info("Duplicate offer detected, updating existing record. offerId={}", target.getId());
        } else {
            log.info("Creating new offer for title='{}', company='{}'", request.getTitle(), request.getCompanyName());
        }

        jobOfferMapper.updateEntityFromRequest(request, target);
        target.setTechnologies(resolveTechnologies(request));
        JobOffer saved = jobOfferPersistenceService.save(target);
        return jobOfferMapper.toResponse(saved);
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

    private Optional<JobOffer> findDuplicate(JobOfferRequestTO request) {
        if (StringUtils.hasText(request.getExternalId())) {
            Optional<JobOffer> byExternalId = jobOfferPersistenceService.findByExternalId(request.getExternalId());
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

    private Set<Technology> resolveTechnologies(JobOfferRequestTO request) {
        if (request.getTechnologies() == null || request.getTechnologies().isEmpty()) {
            return Collections.emptySet();
        }

        return request.getTechnologies().stream()
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .map(this::findOrCreateTechnology)
                .collect(Collectors.toSet());
    }

    private Technology findOrCreateTechnology(String name) {
        return technologyPersistenceService.findByName(name)
                .orElseGet(() -> {
                    Technology technology = new Technology();
                    technology.setName(name);
                    return technologyPersistenceService.save(technology);
                });
    }

    private void validateRequest(JobOfferRequestTO request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new ValidationException("title must not be empty");
        }
        if (!StringUtils.hasText(request.getCompanyName())) {
            throw new ValidationException("companyName must not be empty");
        }
        if (request.getSalaryMin() != null && request.getSalaryMax() != null
                && request.getSalaryMin().compareTo(request.getSalaryMax()) > 0) {
            throw new ValidationException("salaryMin must be less than or equal to salaryMax");
        }
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
