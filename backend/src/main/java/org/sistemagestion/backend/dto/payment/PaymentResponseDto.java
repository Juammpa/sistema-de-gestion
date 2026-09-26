package org.sistemagestion.backend.dto.payment;

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
    private Double amount;

    private boolean mirror;
    private Long supplierId;
    private String supplierName;

    // Solo en pagos espejo: cliente y sucursal que hicieron el pago original
    private String originClientName;
    private String originButcherShopName;
}
