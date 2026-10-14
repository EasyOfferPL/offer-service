package pl.easyoffer.offer_service.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@Entity
@Table(
        name = "TECHNOLOGIES",
        indexes = @Index(
                name = "IDX_TECHNOLOGIES_CANONICAL_NAME",
                columnList = "CANONICAL_NAME"
        )
)
@FieldNameConstants
public class TechnologyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @Column(name = "CANONICAL_NAME", nullable = false, length = 100)
    private String canonicalName;

    @Column(name = "NORMALIZATION_VERSION", nullable = false, length = 20)
    private String normalizationVersion;

    @Column(name = "LEVEL")
    private Integer level;

}
