package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.sistemagestion.backend.model.enums.CutMeatType;

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

    @Enumerated(EnumType.STRING)
    private CutMeatType cutMeatType;

    private Double pricePerKilo;

}
