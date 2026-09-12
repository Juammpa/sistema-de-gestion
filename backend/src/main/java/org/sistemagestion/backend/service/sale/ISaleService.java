package org.sistemagestion.backend.service.sale;

import org.sistemagestion.backend.dto.sales.SaleRequestDto;
import org.sistemagestion.backend.dto.sales.SaleResponseDto;

import java.util.List;

public interface ISaleService {

    SaleResponseDto createSale(SaleRequestDto request);
    SaleResponseDto getSaleById(Long id);
    SaleResponseDto updateSale(Long id, SaleRequestDto request);
    List<SaleResponseDto> getAllSales();
    List<SaleResponseDto> getAllSalesByClient(Long clientId);
    void deleteSaleById(Long id);
}
