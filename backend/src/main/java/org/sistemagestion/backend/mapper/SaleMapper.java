package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.sales.SaleResponseDto;
import org.sistemagestion.backend.model.Client;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.CutMeat;
import org.sistemagestion.backend.model.Sale;
import org.sistemagestion.backend.common.Decimals;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class SaleMapper {

    public Sale toSale(Client client, CutMeat cutMeat, CurrentAccount currentAccount, BigDecimal kilograms, Integer unit) {

        BigDecimal amount = Decimals.multiply(kilograms, cutMeat.getPricePerKilo());

        return Sale.builder()
                .kilograms(Decimals.kilograms(kilograms))
                .unit(unit)
                .client(client)
                .cutMeat(cutMeat)
                .currentAccount(currentAccount)
                .pricePerKilo(cutMeat.getPricePerKilo())
                .cutMeatName(cutMeat.getCutMeatName())
                .amount(amount)
                .date(LocalDate.now())
                .balance(Decimals.moneyOrZero(currentAccount.getCurrentBalance()).add(amount))
                .build();
    }

    public SaleResponseDto toDto(Sale sale) {
        return SaleResponseDto.builder()
                .id(sale.getId())
                .clientId(sale.getClient().getId())
                .date(sale.getDate())
                .cutMeatId(sale.getCutMeat().getId())
                .cutMeatName(sale.getCutMeatName())
                .unit(sale.getUnit())
                .kilograms(sale.getKilograms())
                .pricePerKilo(sale.getPricePerKilo())
                .subtotal(sale.getAmount())
                .build();
    }

}
