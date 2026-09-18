package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.sales.SaleRequestDto;
import org.sistemagestion.backend.dto.sales.SaleResponseDto;
import org.sistemagestion.backend.model.Client;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.CutMeat;
import org.sistemagestion.backend.model.Sale;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class SaleMapper {

    public Sale toSale(SaleRequestDto requestDto, Client client, CutMeat cutMeat, CurrentAccount currentAccount) {

        Double amount = requestDto.getKilograms() * cutMeat.getPricePerKilo();

        return Sale.builder()
                .kilograms(requestDto.getKilograms())
                .unit(requestDto.getUnit())
                .client(client)
                .cutMeat(cutMeat)
                .currentAccount(currentAccount)
                .pricePerKilo(cutMeat.getPricePerKilo())
                .cutMeatName(cutMeat.getCutMeatName())
                .amount(amount)
                .date(LocalDate.now())
                .balance(currentAccount.getCurrentBalance() + amount)
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
