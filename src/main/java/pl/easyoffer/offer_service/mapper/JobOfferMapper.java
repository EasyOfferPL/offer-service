package pl.easyoffer.offer_service.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import pl.easyoffer.offer_service.model.domain.JobOffer;
import pl.easyoffer.offer_service.model.domain.Technology;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.JobOfferResponseTO;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobOfferMapper {

    @Mapping(target = "technologies", expression = "java(mapTechnologies(entity.getTechnologies()))")
    JobOfferResponseTO toResponse(JobOffer entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "technologies", ignore = true)
    @Mapping(target = "active", expression = "java(request.getActive() == null || request.getActive())")
    void updateEntityFromRequest(JobOfferRequestTO request, @MappingTarget JobOffer entity);

    default List<String> mapTechnologies(Set<Technology> technologies) {
        return technologies.stream()
                .map(Technology::getName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }
}
