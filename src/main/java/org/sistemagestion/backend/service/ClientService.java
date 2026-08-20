package org.sistemagestion.backend.service;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.ClientRequestDto;
import org.sistemagestion.backend.dto.ClientResponseDto;
import org.sistemagestion.backend.mapper.ClientMapper;
import org.sistemagestion.backend.model.Client;
import org.sistemagestion.backend.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService implements IClientService{

    private final ClientRepository clientRepository;
    private final ClientMapper mapper;

    @Override
    public ClientResponseDto createClient(ClientRequestDto requestDto) {
        return mapper.toDto(
                clientRepository.save(
                        mapper.toClient(requestDto)));
    }

    @Override
    public ClientResponseDto getById(Long id) {
        return clientRepository.findAll().stream()
                .filter(client -> client.getId().equals(id))
                .map(mapper::toDto)
                .findFirst()
                .orElseThrow(() -> new RuntimeException());
    }

    @Override
    public List<ClientResponseDto> getAllClients() {
        return clientRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public ClientResponseDto updateClient(Long id, ClientRequestDto requestDto) {

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException());

        client.setName(requestDto.getName());
        client.setLastName(requestDto.getLastName());
        client.setPhoneNumber(requestDto.getPhoneNumber());

        return mapper.toDto(clientRepository.save(client));
    }

    @Override
    public void deleteById(Long id) {

       Client client = clientRepository.findById(id)
               .orElseThrow(() -> new RuntimeException());

       clientRepository.delete(client);
    }
}
