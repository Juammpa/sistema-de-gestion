package org.sistemagestion.backend.service.butcher_shop;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopRequestDto;
import org.sistemagestion.backend.dto.butcher_shop.ButcherShopResponseDto;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.ButcherShopMapper;
import org.sistemagestion.backend.model.ButcherShop;
import org.sistemagestion.backend.model.Client;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.repository.ButcherShopRepository;
import org.sistemagestion.backend.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ButcherShopService implements IButcherShopService{

    private final ButcherShopRepository butcherShopRepository;
    private final ClientRepository clientRepository;
    private final ButcherShopMapper mapper;

    @Override
    public ButcherShopResponseDto createButcherShop(ButcherShopRequestDto requestDto) {

        Client client = clientRepository.findByIdAndIsActiveTrue(requestDto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", requestDto.getClientId()));

        ButcherShop butcherShop = mapper.toButcherShop(requestDto);
        butcherShop.setClient(client);

        CurrentAccount currentAccount =  new CurrentAccount();
        currentAccount.setCurrentBalance(0.0);

        butcherShop.setCurrentAccount(currentAccount);

        return mapper.toDto(butcherShopRepository.save(butcherShop));
    }

    @Override
    @Transactional(readOnly = true)
    public ButcherShopResponseDto getById(Long id) {

        ButcherShop butcherShop = butcherShopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Butcher Shop", "id", id));

        return mapper.toDto(butcherShop);

    }

    @Override
    @Transactional
    public ButcherShopResponseDto updateButcherShop(Long id, ButcherShopRequestDto requestDto) {

        Client client = clientRepository.findByIdAndIsActiveTrue(requestDto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client","id", requestDto.getClientId()));

        ButcherShop butcherShop = butcherShopRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Butcher Shop", "id", id));

        butcherShop.setName(requestDto.getName());
        butcherShop.setAddress(requestDto.getAddress());
        butcherShop.setClient(client);

        return mapper.toDto(butcherShopRepository.save(butcherShop));
    }

}
