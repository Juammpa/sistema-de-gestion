package org.sistemagestion.backend.service;

import org.sistemagestion.backend.dto.client.ClientRequestDto;
import org.sistemagestion.backend.dto.client.ClientResponseDto;

import java.util.List;

public interface IClientService {

    ClientResponseDto createClient(ClientRequestDto requestDto);
    ClientResponseDto getById(Long id);
    List<ClientResponseDto> getAllClients();
    ClientResponseDto updateClient(Long id, ClientRequestDto requestDto);
    void deleteById(Long id);
}
