package org.sistemagestion.backend.service.client;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopRequestDto;
import org.sistemagestion.backend.dto.client.ClientCreateRequestDto;
import org.sistemagestion.backend.dto.client.ClientRequestDto;
import org.sistemagestion.backend.dto.client.ClientResponseDto;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.ClientMapper;
import org.sistemagestion.backend.model.Client;
import org.sistemagestion.backend.repository.ClientRepository;
import org.sistemagestion.backend.service.butcher_shop.IButcherShopService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService implements IClientService{

    private final ClientRepository clientRepository;
    private final IButcherShopService butcherShopService;
    private final ClientMapper mapper;

    @Override
    @Transactional
    public ClientResponseDto createClient(ClientCreateRequestDto requestDto) {

        Client clientSaved = clientRepository.save(mapper.toClient(requestDto));

        ButcherShopRequestDto butcherShopRequest = new ButcherShopRequestDto(
                requestDto.getButcherShopName(),
                requestDto.getButcherShopAddress(),
                clientSaved.getId());

        butcherShopService.createButcherShop(butcherShopRequest);

        return mapper.toDto(clientSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponseDto getById(Long id) {

        Client client =  clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        return mapper.toDto(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponseDto> getAllClients() {
        return clientRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ClientResponseDto updateClient(Long id, ClientRequestDto requestDto) {

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

        client.setName(requestDto.getName());
        client.setLastName(requestDto.getLastName());
        client.setPhoneNumber(requestDto.getPhoneNumber());

        return mapper.toDto(clientRepository.save(client));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {

       Client client = clientRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Client", "id", id));

       clientRepository.delete(client);
    }
}
