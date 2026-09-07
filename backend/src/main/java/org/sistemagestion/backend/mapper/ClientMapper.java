package org.sistemagestion.backend.mapper;


import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.client.ClientRequestDto;
import org.sistemagestion.backend.dto.client.ClientResponseDto;
import org.sistemagestion.backend.model.Client;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientMapper {

    private final ButcherShopMapper butcherShopMapper;

    public Client toClient(ClientRequestDto request) {
        return Client.builder()
                .name(request.getName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .build();

    }

    public ClientResponseDto toDto(Client client) {
        return ClientResponseDto.builder()
                .id(client.getId())
                .name(client.getName())
                .lastName(client.getLastName())
                .phoneNumber(client.getPhoneNumber())
                .butcherShops(client.getButcherShops().stream()
                        .map(butcherShopMapper::toDto)
                        .toList()
                )
                .build();
    }
}
