package pl.easyoffer.offer_service.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "OFFER")
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
public class OfferEntity extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private OfferType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "OFFER_CONTEXT", columnDefinition = "json") //todo jsonb for postgres
    private OfferContext offerContext;

    private boolean actual;

}
