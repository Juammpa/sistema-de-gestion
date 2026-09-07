package org.sistemagestion.backend.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopResponseDto;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ClientResponseDto {

    private Long id;
    private String name;
    private String lastName;
    private String phoneNumber;
    private List<ButcherShopResponseDto> butcherShops;
}
