package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.CutMeatType;

@Entity
@Table(name = "cut_meats")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CutMeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CutMeatType cutMeatType;

    private Double pricePerKilo;

}
