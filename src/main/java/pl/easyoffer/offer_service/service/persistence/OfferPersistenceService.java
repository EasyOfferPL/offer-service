package pl.easyoffer.offer_service.service.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.repository.OfferRepository;
import pl.easyoffer.offer_service.repository.projection.*;
import pl.easyoffer.offer_service.service.calculator.DataCompletenessInput;
import pl.easyoffer.offer_service.service.normalization.SalaryComparabilityInput;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OfferPersistenceService {

    private final OfferRepository offerRepository;

    public Page<OfferEntity> findAll(Pageable pageable) {
        return offerRepository.findAll(pageable);
    }

    public Optional<OfferEntity> findById(Long id) {
        return offerRepository.findById(id);
    }

    public List<OfferEntity> search(Specification<OfferEntity> specification) {
        return offerRepository.findAll(specification);
    }

    public Page<OfferEntity> search(Specification<OfferEntity> specification, Pageable pageable) {
        return offerRepository.findAll(specification, pageable);
    }

    public OfferEntity save(OfferEntity offer) {
        return offerRepository.save(offer);
    }

    public Optional<OfferEntity> findBySourceAndExternalId(String source, String externalId) {
        return offerRepository.findBySourceAndExternalId(source, externalId);
    }

    public Optional<OfferEntity> findBySourceAndMissingExternalIdAndDeduplicationFingerprint(
            String source,
            String deduplicationFingerprint
    ) {
        return offerRepository.findFirstBySourceAndExternalIdIsNullAndDeduplicationFingerprintOrderByIdAsc(
                source, deduplicationFingerprint
        );
    }

    public long countOffers() {
        return offerRepository.count();
    }

    public List<String> findCrossSourceDuplicateFingerprints(Pageable pageable) {
        return offerRepository.findCrossSourceDuplicateFingerprints(pageable);
    }

    public long countCrossSourceDuplicateGroups() {
        return offerRepository.countCrossSourceDuplicateGroups();
    }

    public long countCrossSourceDuplicateGroupMembers() {
        return offerRepository.countCrossSourceDuplicateGroupMembers();
    }

    public List<DeduplicationAuditOfferProjection> findCrossSourceDuplicateGroupMembers(
            List<String> fingerprints
    ) {
        if (fingerprints.isEmpty()) {
            return List.of();
        }
        return offerRepository.findCrossSourceDuplicateGroupMembers(fingerprints);
    }

    public List<CategoryStatisticProjection> getCategoryStatistics(Pageable pageable) {
        return offerRepository.getCategoryStatistics(pageable);
    }

    public List<CategoryProjection> getCategories() {
        return offerRepository.getCategories();
    }

    public List<OfferEntity> findForSalaryAnalytics(String categoryName, String experienceLevel) {
        return offerRepository.findForSalaryAnalytics(categoryName, experienceLevel);
    }

    public List<OfferEntity> findForTechnologyAnalytics(
            String categoryName,
            List<String> sources,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        if (sources.isEmpty()) {
            return offerRepository.findForTechnologyAnalytics(categoryName, dateFrom, dateTo);
        }
        return offerRepository.findForTechnologyAnalytics(categoryName, sources, dateFrom, dateTo);
    }

    public Stream<SalaryComparabilityInput> streamSalaryComparabilityInputs() {
        return offerRepository.streamSalaryComparabilityInputs()
                .map(OfferPersistenceService::toSalaryComparabilityInput);
    }

    public Stream<DataCompletenessInput> streamDataCompletenessInputs(
            List<String> sources,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        Stream<DataCompletenessInputProjection> projections = sources.isEmpty()
                ? offerRepository.streamDataCompletenessInputs(dateFrom, dateTo)
                : offerRepository.streamDataCompletenessInputs(sources, dateFrom, dateTo);
        return projections.map(OfferPersistenceService::toDataCompletenessInput);
    }

    public List<OfferEntity> findForSalaryTrend(
            String categoryName,
            String currency,
            String employmentType,
            SalaryType salaryType,
            String experienceLevel,
            LocalDateTime dateFrom,
            LocalDateTime dateTo
    ) {
        return offerRepository.findForSalaryTrend(
                categoryName, currency, employmentType, salaryType,
                experienceLevel, dateFrom, dateTo
        );
    }

    private static SalaryComparabilityInput toSalaryComparabilityInput(
            SalaryComparabilityInputProjection projection
    ) {
        return new SalaryComparabilityInput(
                projection.getSalaryMin(),
                projection.getSalaryMax(),
                projection.getCurrency(),
                projection.getSalaryUnit(),
                projection.getEmploymentType(),
                projection.getSalaryType()
        );
    }

    private static DataCompletenessInput toDataCompletenessInput(
            DataCompletenessInputProjection projection
    ) {
        return new DataCompletenessInput(
                projection.getSource(),
                new SalaryComparabilityInput(
                        projection.getSalaryMin(),
                        projection.getSalaryMax(),
                        projection.getCurrency(),
                        projection.getSalaryUnit(),
                        projection.getEmploymentType(),
                        projection.getSalaryType()
                ),
                projection.getTechnologyCount(),
                projection.getExperienceLevel(),
                projection.getLocation(),
                projection.getWorkMode(),
                projection.getPublishedAt()
        );
    }

}
