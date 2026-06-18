package pl.easyoffer.offer_service.util;

import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import pl.easyoffer.offer_service.model.entity.AbstractAuditingEntity;
import pl.easyoffer.offer_service.model.entity.OfferEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
public final class OfferSpecificationBuilder {

    private Specification<OfferEntity> spec;

    public static OfferSpecificationBuilder builder() {
        return new OfferSpecificationBuilder();
    }

    public OfferSpecificationBuilder withIds(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.id).in(ids));
        }
        return this;
    }

    public OfferSpecificationBuilder withExternalIds(List<String> externalIds) {
        if (!CollectionUtils.isEmpty(externalIds)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.externalId).in(externalIds));
        }
        return this;
    }

    public OfferSpecificationBuilder withTitles(List<String> titles) {
        if (!CollectionUtils.isEmpty(titles)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.title).in(titles));
        }
        return this;
    }

    public OfferSpecificationBuilder withCompanyNames(List<String> companyNames) {
        if (!CollectionUtils.isEmpty(companyNames)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.companyName).in(companyNames));
        }
        return this;
    }

    public OfferSpecificationBuilder withLocations(List<String> locations) {
        if (!CollectionUtils.isEmpty(locations)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.location).in(locations));
        }
        return this;
    }

    public OfferSpecificationBuilder withExperienceLevels(List<String> experienceLevels) {
        if (!CollectionUtils.isEmpty(experienceLevels)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.experienceLevel).in(experienceLevels));
        }
        return this;
    }

    public OfferSpecificationBuilder withEmploymentTypes(List<String> employmentTypes) {
        if (!CollectionUtils.isEmpty(employmentTypes)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.employmentType).in(employmentTypes));
        }
        return this;
    }

    public OfferSpecificationBuilder withWorkModes(List<String> workModes) {
        if (!CollectionUtils.isEmpty(workModes)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.workMode).in(workModes));
        }
        return this;
    }

    public OfferSpecificationBuilder withSalaryBetween(BigDecimal salaryFrom, BigDecimal salaryTo) {
        if (Objects.nonNull(salaryFrom)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get(OfferEntity.Fields.salaryMin), salaryFrom));
        }
        if (Objects.nonNull(salaryTo)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get(OfferEntity.Fields.salaryMax), salaryTo));
        }
        return this;
    }

    public OfferSpecificationBuilder withSalaryUnits(List<String> salaryUnits) {
        if (!CollectionUtils.isEmpty(salaryUnits)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.salaryUnit).in(salaryUnits));
        }
        return this;
    }

    public OfferSpecificationBuilder withCurrencies(List<String> currencies) {
        if (!CollectionUtils.isEmpty(currencies)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.currency).in(currencies));
        }
        return this;
    }

    public OfferSpecificationBuilder withSources(List<String> sources) {
        if (!CollectionUtils.isEmpty(sources)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.source).in(sources));
        }
        return this;
    }

    public OfferSpecificationBuilder withUrls(List<String> urls) {
        if (!CollectionUtils.isEmpty(urls)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.url).in(urls));
        }
        return this;
    }

    public OfferSpecificationBuilder withLanguages(List<String> languages) {
        if (!CollectionUtils.isEmpty(languages)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.language).in(languages));
        }
        return this;
    }

    public OfferSpecificationBuilder withCreatedAtBetween(LocalDateTime from, LocalDateTime to) {
        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(AbstractAuditingEntity.Fields.createdAt), from, to));
        } else if (Objects.nonNull(from)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get(AbstractAuditingEntity.Fields.createdAt), from));
        } else if (Objects.nonNull(to)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get(AbstractAuditingEntity.Fields.createdAt), to));
        }
        return this;
    }

    public OfferSpecificationBuilder withUpdatedAtBetween(LocalDateTime from, LocalDateTime to) {
        if (Objects.nonNull(from) && Objects.nonNull(to)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(AbstractAuditingEntity.Fields.updatedAt), from, to));
        } else if (Objects.nonNull(from)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get(AbstractAuditingEntity.Fields.updatedAt), from));
        } else if (Objects.nonNull(to)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get(AbstractAuditingEntity.Fields.updatedAt), to));
        }
        return this;
    }

    public OfferSpecificationBuilder withTechnologies(List<String> technologies) {
        if (!CollectionUtils.isEmpty(technologies)) {
            wrapSpecification((root, query, criteriaBuilder) ->
                    root.get(OfferEntity.Fields.technologies).in(technologies));
        }
        return this;
    }

    public Specification<OfferEntity> build() {
        return spec;
    }

    private void wrapSpecification(Specification<OfferEntity> spec) {
        if (Objects.isNull(this.spec)) {
            this.spec = spec;
        } else {
            this.spec.and(spec);
        }
    }

}
