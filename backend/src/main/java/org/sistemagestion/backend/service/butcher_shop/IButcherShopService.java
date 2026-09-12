package org.sistemagestion.backend.service.butcher_shop;

import org.sistemagestion.backend.dto.butcher_shop.ButcherShopRequestDto;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopResponseDto;

import java.util.List;

public interface IButcherShopService {

    ButcherShopResponseDto createButcherShop(ButcherShopRequestDto requestDto);
    ButcherShopResponseDto getById(Long id);
    List<ButcherShopResponseDto> getByClient(Long clientId);
    List<ButcherShopResponseDto> getAllButcherShops();
    ButcherShopResponseDto updateButcherShop(Long id, ButcherShopRequestDto requestDto);
    void deleteById(Long id);

}
