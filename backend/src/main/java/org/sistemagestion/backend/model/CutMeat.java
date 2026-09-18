package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;

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
    private Double pricePerKilo;

}
