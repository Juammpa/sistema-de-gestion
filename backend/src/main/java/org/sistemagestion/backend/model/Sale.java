package org.sistemagestion.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "sales")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Sale extends Movement{

    private Double kilograms;
    private Integer unit;

    @ManyToOne
    @JoinColumn(name = "cut_meat_id", nullable = false)
    private CutMeat cutMeat;
}
