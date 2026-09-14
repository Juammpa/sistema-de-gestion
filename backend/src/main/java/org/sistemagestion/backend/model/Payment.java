package org.sistemagestion.backend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.sistemagestion.backend.model.enums.PaymentType;

@Entity
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
public class Payment extends Movement{

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

}
