package org.sistemagestion.backend.service.client;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.client.ClientRequestDto;
import org.sistemagestion.backend.dto.client.ClientResponseDto;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
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

        Client client =  clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        return mapper.toDto(client);
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
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        client.setName(requestDto.getName());
        client.setLastName(requestDto.getLastName());
        client.setPhoneNumber(requestDto.getPhoneNumber());

        return mapper.toDto(clientRepository.save(client));
    }

    @Override
    public void deleteById(Long id) {

       Client client = clientRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

       clientRepository.delete(client);
    }
}
