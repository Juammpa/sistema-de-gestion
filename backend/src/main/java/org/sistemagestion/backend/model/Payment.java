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

    // Proveedor destino (solo en pagos de clientes de tipo SUPPLIER_TRANSFER)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    // Pago original que dio origen a este (solo en el "pago espejo" de la cuenta del proveedor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_payment_id")
    private Payment originPayment;
}
