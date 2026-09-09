package org.sistemagestion.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;
import org.sistemagestion.backend.model.enums.PaymentType;

@Entity
@Table(name = "payments")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Payment extends Movement{

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
}
