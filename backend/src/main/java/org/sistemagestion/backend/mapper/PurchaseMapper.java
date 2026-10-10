package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.purchase.PurchaseRequestDto;
import org.sistemagestion.backend.dto.purchase.PurchaseResponseDto;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Purchase;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.common.Decimals;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class PurchaseMapper {

    public Purchase toPurchase(PurchaseRequestDto requestDto, CurrentAccount currentAccount) {

        BigDecimal amount = Decimals.multiply(requestDto.getKilograms(), requestDto.getPricePerKilo());

        return Purchase.builder()
                .purchaseType(requestDto.getPurchaseType())
                .specie(Specie.BOVINE)  // TODO: Obtener especie mediante el proveedor
                .kilograms(Decimals.kilograms(requestDto.getKilograms()))
                .unit(requestDto.getUnit())
                .pricePerKilo(Decimals.money(requestDto.getPricePerKilo()))
                .currentAccount(currentAccount)
                .date(LocalDate.now())
                .amount(amount)
                .balance(Decimals.moneyOrZero(currentAccount.getCurrentBalance()).add(amount))
                .build();
    }

    public PurchaseResponseDto toDto(Purchase purchase) {
        return PurchaseResponseDto.builder()
                .id(purchase.getId())
                .date(purchase.getDate())
                .currentAccountId(purchase.getCurrentAccount().getId())
                .purchaseType(purchase.getPurchaseType())
                .unit(purchase.getUnit())
                .kilograms(purchase.getKilograms())
                .pricePerKilo(purchase.getPricePerKilo())
                .subtotal(purchase.getAmount())
                .build();
    }

}
