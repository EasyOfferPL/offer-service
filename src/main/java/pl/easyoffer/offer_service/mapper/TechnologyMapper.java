package pl.easyoffer.offer_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pl.easyoffer.offer_service.model.dto.TechnologyTO;
import pl.easyoffer.offer_service.model.dto.justjoinit.JustJoinItSkill;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TechnologyMapper {

    TechnologyMapper INSTANCE = Mappers.getMapper(TechnologyMapper.class);

    List<TechnologyTO> map(List<JustJoinItSkill> justJoinItSkills);

    TechnologyTO map(JustJoinItSkill justJoinItSkill);

}
