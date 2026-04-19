package pl.easyoffer.offer_service.util;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;

public final class OfferSpecificationBuilder {

    private OfferSpecificationBuilder() {
    }

    public static Specification<JobOfferEntity> build(String technology, String location, String experienceLevel) {
        Specification<JobOfferEntity> specification = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(technology)) {
            String normalized = technology.trim().toLowerCase();
            specification = specification.and((root, query, cb) -> {
                var technologies = root.join("technologies", JoinType.LEFT);
                query.distinct(true);
                return cb.equal(cb.lower(technologies.get("name")), normalized);
            });
        }

        if (StringUtils.hasText(location)) {
            String normalized = location.trim().toLowerCase();
            specification = specification.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("location")), "%" + normalized + "%"));
        }

        if (StringUtils.hasText(experienceLevel)) {
            String normalized = experienceLevel.trim().toLowerCase();
            specification = specification.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("experienceLevel")), normalized));
        }

        return specification;
    }
}
