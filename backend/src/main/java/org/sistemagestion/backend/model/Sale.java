package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "sales")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Sale extends Movement{

    @Column(precision = 10, scale = 3)
    private BigDecimal kilograms;

    private Integer unit;
    private String cutMeatName;

    @Column(name = "price_per_kilo", precision = 12, scale = 2)
    private BigDecimal pricePerKilo;

    @ManyToOne
    @JoinColumn(name = "cut_meat_id", nullable = false)
    private CutMeat cutMeat;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}
