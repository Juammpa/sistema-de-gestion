package org.sistemagestion.backend.mapper;

import org.sistemagestion.backend.dto.butcher_shop.ButcherShopRequestDto;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopResponseDto;
import org.sistemagestion.backend.model.ButcherShop;

import org.springframework.stereotype.Component;

@Component
public class ButcherShopMapper {

    public ButcherShop toButcherShop(ButcherShopRequestDto requestDto) {
        return ButcherShop.builder()
                .name(requestDto.getName().trim())
                .address(requestDto.getAddress().trim())
                .build();
    }

    public ButcherShopResponseDto toDto(ButcherShop butcherShop) {
        return ButcherShopResponseDto.builder()
                .id(butcherShop.getId())
                .name(butcherShop.getName())
                .address(butcherShop.getAddress())
                .clientId(butcherShop.getClient().getId())
                .clientName(String.format("%s %s", butcherShop.getClient().getName(), butcherShop.getClient().getLastName()))
                .currentAccountId(butcherShop.getCurrentAccount().getId())
                .currentBalance(butcherShop.getCurrentAccount().getCurrentBalance())
                .build();
    }

}
