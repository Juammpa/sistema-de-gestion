package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.sistemagestion.backend.model.enums.Specie;

import java.math.BigDecimal;

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

    private String cutMeatName;

    @Column(name = "price_per_kilo", precision = 12, scale = 2)
    private BigDecimal pricePerKilo;
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private Specie specie;
}
