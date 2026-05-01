package pl.easyoffer.offer_service.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.util.StringUtils;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.JobOfferResponseTO;
import pl.easyoffer.offer_service.model.entity.JobOfferEntity;
import pl.easyoffer.offer_service.model.entity.TechnologyEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobOfferMapper {

    JobOfferMapper INSTANCE = Mappers.getMapper(JobOfferMapper.class);

    @Mapping(target = "technologies", expression = "java(mapTechnologies(entity.getTechnologies()))")
    JobOfferResponseTO toResponse(JobOfferEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "technologies", ignore = true)
    void updateEntity(JobOfferRequestTO src, @MappingTarget JobOfferEntity dst);

    default List<String> mapTechnologies(Set<TechnologyEntity> technologies) {
        return technologies.stream()
                .map(TechnologyEntity::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    @AfterMapping
    default void normalizeRequest(@MappingTarget JobOfferRequestTO request) {
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
