package org.sistemagestion.backend.dto.butcher_shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ButcherShopResponseDto {

    private Long id;
    private String name;
    private String address;
    private Long clientId;
    private String clientName;
    private Long currentAccountId;
    private Double currentBalance;
}
