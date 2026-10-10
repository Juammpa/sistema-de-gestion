package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.payment.PaymentRequestDto;
import org.sistemagestion.backend.dto.payment.PaymentResponseDto;
import org.sistemagestion.backend.common.Decimals;
import org.sistemagestion.backend.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class PaymentMapper {

    public Payment toPayment(PaymentRequestDto requestDto, CurrentAccount currentAccount) {

        BigDecimal amount = Decimals.money(requestDto.getAmount());

        return Payment.builder()
                .paymentType(requestDto.getPaymentType())
                .amount(amount)
                .date(LocalDate.now())
                .currentAccount(currentAccount)
                .balance(Decimals.moneyOrZero(currentAccount.getCurrentBalance()).subtract(amount))
                .build();
    }

    public PaymentResponseDto toDto(Payment payment) {
        return toDto(payment, null);
    }

    // originShop: Sucursal del cliente que hizo el pago original (solo pagos espejo, puede ser null)
    public PaymentResponseDto toDto(Payment payment, ButcherShop originShop) {

        Supplier supplier = payment.getSupplier();

        return PaymentResponseDto.builder()
                .id(payment.getId())
                .currentAccountId(payment.getCurrentAccount().getId())
                .date(payment.getDate())
                .paymentType(payment.getPaymentType())
                .amount(payment.getAmount())
                .mirror(payment.getOriginPayment() != null)
                .supplierId(supplier != null ? supplier.getId() : null)
                .supplierName(supplier != null ? supplier.getCompanyName() : null)
                .originClientName(originShop != null ?  fullName(originShop.getClient()): null)
                .originButcherShopName(originShop != null ? originShop.getName() : null)
                .build();
    }

    // "Apellido Nombre", como en el listado de clientes y en la caja
    private String fullName(Client client) {
        return String.format("%s %s", client.getLastName(), client.getName());
    }
}
