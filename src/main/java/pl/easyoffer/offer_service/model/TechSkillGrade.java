package pl.easyoffer.offer_service.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TechSkillGrade {
    ADVANCED("advanced"); //todo add all

    private final String name;
}
