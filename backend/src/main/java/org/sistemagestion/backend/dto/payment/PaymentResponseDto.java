package org.sistemagestion.backend.dto.payment;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.model.enums.PaymentType;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class PaymentResponseDto {

    private Long id;
    private Long currentAccountId;
    private LocalDate date;
    private PaymentType paymentType;
    private BigDecimal amount;

    private boolean mirror;
    private Long supplierId;
    private String supplierName;

    // Solo en rechazos de cheque: ID del cheque rechazado
    private Long rejectedPaymentId;

    // Solo en cheques: ya tiene un rechazo registrado
    private boolean rejected;

    // Solo en pagos espejo: cliente y sucursal que hicieron el pago original
    private String originClientName;
    private String originButcherShopName;
}
