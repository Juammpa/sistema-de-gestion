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
        return mapper.toDto(findActive(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponseDto> getAllClients() {
        return clientRepository.findByIsActiveTrue().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ClientResponseDto updateClient(Long id, ClientRequestDto requestDto) {

        Client client = findActive(id);

        client.setName(requestDto.getName());
        client.setLastName(requestDto.getLastName());
        client.setPhoneNumber(requestDto.getPhoneNumber());

        return mapper.toDto(clientRepository.save(client));
    }

    // Borrado logico: el cliente deja de aparecer, pero sus sucursales, cuentas y movimientos
    // se conservan (la Caja y los reportes dependen de ese historial)
    @Override
    @Transactional
    public void deleteById(Long id) {

       Client client = findActive(id);

       client.setIsActive(false);
       clientRepository.save(client);
    }

    // Un cliente eliminado se trata como inexistente
    private Client findActive(Long id) {
        return clientRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client","id",id));
    }
}
