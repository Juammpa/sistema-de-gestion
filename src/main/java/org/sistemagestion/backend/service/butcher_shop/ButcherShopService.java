package org.sistemagestion.backend.service.butcher_shop;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopRequestDto;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopResponseDto;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.ButcherShopMapper;
import org.sistemagestion.backend.model.ButcherShop;
import org.sistemagestion.backend.model.ButcherShopCurrentAccount;
import org.sistemagestion.backend.model.Client;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ButcherShopService implements IButcherShopService{

    private final ButcherShopRepository butcherShopRepository;
    private final ClientRepository clientRepository;
    private final ButcherShopMapper mapper;

    @Override
    public ButcherShopResponseDto createButcherShop(ButcherShopRequestDto requestDto) {

        Client client = clientRepository.findById(requestDto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", requestDto.getClientId()));

        ButcherShop butcherShop = mapper.toButcherShop(requestDto);
        butcherShop.setClient(client);

        ButcherShopCurrentAccount currentAccount =  new ButcherShopCurrentAccount();
        currentAccount.setCurrentBalance(0.0);

        butcherShop.setCurrentAccount(currentAccount);

        return mapper.toDto(butcherShopRepository.save(butcherShop));
    }

    @Override
    public ButcherShopResponseDto getById(Long id) {

        ButcherShop butcherShop = butcherShopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Butcher Shop", "id", id));

        return mapper.toDto(butcherShop);

    }

    @Override
    public List<ButcherShopResponseDto> getByClient(Long clientId) {
        return butcherShopRepository.findByClientId(clientId).stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public List<ButcherShopResponseDto> getAllButcherShops() {
        return butcherShopRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public ButcherShopResponseDto updateButcherShop(Long id, ButcherShopRequestDto requestDto) {

        Client client = clientRepository.findById(requestDto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client","id", requestDto.getClientId()));

        ButcherShop butcherShop = butcherShopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Butcher Shop", "id", id));

        butcherShop.setName(requestDto.getName());
        butcherShop.setAddress(requestDto.getAddress());
        butcherShop.setClient(client);

        return mapper.toDto(butcherShopRepository.save(butcherShop));
    }

    @Override
    public void deleteById(Long id) {

        ButcherShop butcherShop = butcherShopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Butcher Shop", "id", id));

        butcherShopRepository.delete(butcherShop);
    }
}
