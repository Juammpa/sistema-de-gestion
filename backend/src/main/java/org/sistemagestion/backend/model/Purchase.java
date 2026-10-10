package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sistemagestion.backend.model.enums.PurchaseType;
import org.sistemagestion.backend.model.enums.Specie;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchases")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Purchase extends Movement{

    @Enumerated(EnumType.STRING)
    private Specie specie;

    @Enumerated(EnumType.STRING)
    private PurchaseType purchaseType;

    private Integer unit;

    @Column(precision = 10, scale = 3)
    private BigDecimal kilograms;

    @Column(name = "price_per_kilo", precision = 12, scale = 2)
    private BigDecimal pricePerKilo;

}
