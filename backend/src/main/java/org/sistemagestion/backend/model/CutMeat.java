package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.TenantId;
import org.sistemagestion.backend.model.enums.Specie;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cut_meats")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CutMeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dueño del registro
    @TenantId
    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    private String cutMeatName;

    @Column(name = "price_per_kilo", precision = 12, scale = 2)
    private BigDecimal pricePerKilo;
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private Specie specie;
}
