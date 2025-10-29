package pl.easyoffer.offer_service.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "OFFER")
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class OfferEntity extends AbstractAuditingEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private OfferType type;

    @Type(JsonType.class)
    @Column(name = "OFFER_CONTEXT")
    private OfferContext offerContext;

    private boolean actual;

}
