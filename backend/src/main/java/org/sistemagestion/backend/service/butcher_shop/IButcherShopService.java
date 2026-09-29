package org.sistemagestion.backend.service.butcher_shop;

import org.sistemagestion.backend.dto.butcher_shop.ButcherShopRequestDto;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopResponseDto;


public interface IButcherShopService {

    ButcherShopResponseDto createButcherShop(ButcherShopRequestDto requestDto);
    ButcherShopResponseDto getById(Long id);
    ButcherShopResponseDto updateButcherShop(Long id, ButcherShopRequestDto requestDto);

}
