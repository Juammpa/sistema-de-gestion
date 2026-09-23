package org.sistemagestion.backend.service.client;

import org.sistemagestion.backend.dto.client.ClientCreateRequestDto;
import org.sistemagestion.backend.dto.client.ClientRequestDto;
import org.sistemagestion.backend.dto.client.ClientResponseDto;

import java.util.List;

public interface IClientService {

    ClientResponseDto createClient(ClientCreateRequestDto requestDto);
    ClientResponseDto getById(Long id);
    List<ClientResponseDto> getAllClients();
    ClientResponseDto updateClient(Long id, ClientRequestDto requestDto);
    void deleteById(Long id);
}
