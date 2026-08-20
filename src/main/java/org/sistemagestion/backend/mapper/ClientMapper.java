package org.sistemagestion.backend.mapper;


import org.sistemagestion.backend.dto.ClientRequestDto;
import org.sistemagestion.backend.dto.ClientResponseDto;
import org.sistemagestion.backend.model.Client;

public class ClientMapper {

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
                .build();
    }
}
