package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.movement.MovementResponseDto;
import org.sistemagestion.backend.model.Movement;
import org.sistemagestion.backend.model.Payment;
import org.sistemagestion.backend.model.Sale;
import org.sistemagestion.backend.model.enums.MovementType;
import org.springframework.stereotype.Component;

@Component
public class MovementMapper {

    public MovementResponseDto toDto(Movement movement) {

        MovementResponseDto.MovementResponseDtoBuilder builder =
                MovementResponseDto.builder()
                        .id(movement.getId())
                        .date(movement.getDate())
                        .amount(movement.getAmount())
                        .balance(movement.getBalance());

        if (movement instanceof Sale sale) {
            return builder
                    .type(MovementType.SALE)
                    .cutMeatType(sale.getCutMeat().getCutMeatType())
                    .unit(sale.getUnit())
                    .kilograms(sale.getKilograms())
                    .pricePerKilo(sale.getCutMeat().getPricePerKilo())
                    .build();
        }

        if (movement instanceof Payment payment) {
            return builder
                    .type(MovementType.PAYMENT)
                    .paymentType(payment.getPaymentType())
                    .build();
        }

        throw new IllegalStateException("Tipo de movimiento no soportado: " + movement.getClass());

    }

}
