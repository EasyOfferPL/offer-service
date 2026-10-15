package pl.easyoffer.offer_service.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;
import pl.easyoffer.offer_service.model.JobOfferSourceType;
import pl.easyoffer.offer_service.model.SalaryType;
import pl.easyoffer.offer_service.model.entity.OfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;
import pl.easyoffer.offer_service.model.to.OfferRequestTO;
import pl.easyoffer.offer_service.model.to.OfferResponseTO;

import java.util.*;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OfferMapper {

    OfferMapper INSTANCE = Mappers.getMapper(OfferMapper.class);

    @Mapping(target = "technologies", expression = "java(mapTechnologies(entity.getTechnologies()))")
    OfferResponseTO toResponse(OfferEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "technologies", ignore = true)
    void updateEntity(OfferRequestTO src, @MappingTarget OfferEntity dst);

    default List<String> mapTechnologies(Set<TechnologyEntity> technologies) {
        return technologies.stream()
                .map(technology -> Optional.ofNullable(technology.getCanonicalName())
                        .orElse(technology.getName()))
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    @AfterMapping
    default void normalizeRequest(@MappingTarget OfferRequestTO request) {
        request.setExternalId(trimToNull(request.getExternalId()));
        request.setTitle(trimToNull(request.getTitle()));
        request.setCompanyName(trimToNull(request.getCompanyName()));
        request.setLocation(trimToNull(request.getLocation()));
        request.setExperienceLevel(trimToNull(request.getExperienceLevel()));
        request.setEmploymentType(trimToNull(request.getEmploymentType()));
        request.setWorkMode(trimToNull(request.getWorkMode()));
        request.setCurrency(trimToNull(request.getCurrency()));
        request.setSalaryType(Optional.ofNullable(request.getSalaryType())
                .orElse(SalaryType.UNKNOWN));
        request.setSource(defaultSource(request.getSource()));
        request.setUrl(trimToNull(request.getUrl()));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String defaultSource(String value) {
        String source = trimToNull(value);
        return source == null ? JobOfferSourceType.MANUAL.name() : source;
    }


}
