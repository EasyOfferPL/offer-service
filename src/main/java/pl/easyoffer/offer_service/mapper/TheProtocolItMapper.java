package pl.easyoffer.offer_service.mapper;

import org.apache.logging.log4j.util.Strings;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.util.CollectionUtils;
import pl.easyoffer.offer_service.model.dto.JobOfferRequestTO;
import pl.easyoffer.offer_service.model.dto.TechnologyTO;
import pl.easyoffer.offer_service.model.dto.theprotocolit.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TheProtocolItMapper {

    TheProtocolItMapper INSTANCE = Mappers.getMapper(TheProtocolItMapper.class);

    @Mapping(source = "id", target = "externalId")
    @Mapping(source = "technologies", target = "technologies", qualifiedByName = "mapTechnologies")
    @Mapping(source = "employer", target = "companyName")
    @Mapping(source = "workplace", target = "location", qualifiedByName = "mapLocation")
    @Mapping(source = "positionLevels", target = "experienceLevel", qualifiedByName = "mapExperienceLevel")
    @Mapping(source = "typesOfContracts", target = "employmentType", qualifiedByName = "mapEmploymentType")
    @Mapping(source = "workModes", target = "workMode", qualifiedByName = "mapWorkModes")
    @Mapping(source = "publicationDateUtc", target = "publishedAt")
    @Mapping(source = "isSupportingUkraine", target = "openToHireUkrainians")
    JobOfferRequestTO map(TheProtocolOffer src);

    @Named("mapTechnologies")
    default List<TechnologyTO> mapTechnologies(List<String> rawTechnologies) {
        if (CollectionUtils.isEmpty(rawTechnologies)) {
            return List.of();
        }
        return rawTechnologies.stream()
                .map(technology -> new TechnologyTO(technology, null))
                .toList();
    }

    @Named("mapLocation")
    default String mapLocation(List<TheProtocolWorkplace> theProtocolWorkplace) {
        return Optional.ofNullable(theProtocolWorkplace.getFirst())
                .map(TheProtocolWorkplace::getCity)
                .orElse(null);

    }

    @Named("mapExperienceLevel")
    default String mapExperienceLevel(List<TheProtocolPositionLevel> theProtocolPositionLevel) {
        return Optional.ofNullable(theProtocolPositionLevel.getFirst())
                .map(TheProtocolPositionLevel::getValue)
                .orElse(null);
    }

    @Named("mapEmploymentType")
    default String mapEmploymentType(List<TheProtocolContract> contracts) {
        if (CollectionUtils.isEmpty(contracts)) {
            return null;
        }

        return contracts.stream()
                .map(TheProtocolContract::getId)
                .filter(Objects::nonNull)
                .map(TheProtocolItEmploymentType::getNameByValue)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    @Named("mapWorkModes")
    default String mapWorkModes(List<String> theProtocolContract) {
        return Optional.ofNullable(theProtocolContract.getFirst())
                .map(TheProtocolItWorkMode::getNameByValue)
                .orElse(null);
    }

    @AfterMapping
    default void determineSalaryParameters(TheProtocolOffer src, @MappingTarget JobOfferRequestTO target) {
        Optional.ofNullable(src.getSalary())
                .ifPresent(salary -> {
                    target.setSalaryMin(salary.getFrom());
                    target.setSalaryMax(salary.getTo());
                    target.setSalaryUnit(determineSalaryUnit(salary));
                    target.setCurrency(determineSalaryCurrency(salary));
                });
    }

    private static String determineSalaryUnit(TheProtocolSalary salary) {
        return Optional.ofNullable(salary.getTimeUnit())
                .map(TheProtocolTimeUnit::getShortForm)
                .map(TheProtocolItTimeUnitType::getNameByValue)
                .orElse(null);
    }

    //todo remove PLN constant
    private static String determineSalaryCurrency(TheProtocolSalary salary) {
        String pln = "PLN";
        String plnSymbol = "zł";
        if (Strings.isNotBlank(salary.getCurrency())) {
            if (plnSymbol.equals(salary.getCurrency())) {
                return pln;
            }
            return salary.getCurrency();
        } else if (Strings.isNotBlank(salary.getCurrencySymbol())) {
            if (plnSymbol.equals(salary.getCurrencySymbol())) {
                return pln;
            }
            return salary.getCurrencySymbol();
        }
        return pln;
    }

}
