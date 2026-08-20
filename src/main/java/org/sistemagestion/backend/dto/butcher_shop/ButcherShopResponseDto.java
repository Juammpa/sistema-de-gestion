package org.sistemagestion.backend.dto.butcher_shop;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ButcherShopResponseDto {

    private Long id;
    private String name;
    private String address;
    private Long clientName;
    private Long currentAccountId;
}
