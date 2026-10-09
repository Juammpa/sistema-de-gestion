package org.sistemagestion.backend.service.butcher_shop;

import org.sistemagestion.backend.dto.butcher_shop.ButcherShopRequestDto;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopResponseDto;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopUpdateRequestDto;


public interface IButcherShopService {

    ButcherShopResponseDto createButcherShop(ButcherShopRequestDto requestDto);
    ButcherShopResponseDto getById(Long id);
    ButcherShopResponseDto updateButcherShop(Long id, ButcherShopUpdateRequestDto requestDto);
    void deleteButcherShop(Long id);

    boolean existsByName(Long clientId, String name, Long excludeId);
}
