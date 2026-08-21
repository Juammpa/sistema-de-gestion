package org.sistemagestion.backend.service.supplier;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.supplier.SupplierRequestDto;
import org.sistemagestion.backend.dto.supplier.SupplierResponseDto;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.SupplierMapper;
import org.sistemagestion.backend.model.Supplier;
import org.sistemagestion.backend.model.SupplierCurrentAccount;
import org.sistemagestion.backend.repository.SupplierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService implements ISupplierService{

    private final SupplierRepository supplierRepository;
    private final SupplierMapper mapper;

    @Override
    public SupplierResponseDto createSupplier(SupplierRequestDto request) {

        Supplier supplier = mapper.toSupplier(request);

        SupplierCurrentAccount currentAccount = new SupplierCurrentAccount();
        currentAccount.setCurrentBalance(0.0);

        supplier.setCurrentAccount(currentAccount);

        return mapper.toDto(supplierRepository.save(supplier));
    }

    @Override
    public SupplierResponseDto getById(Long id) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        return mapper.toDto(supplier);
    }

    @Override
    public List<SupplierResponseDto> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public SupplierResponseDto updateSupplier(Long id, SupplierRequestDto request) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        supplier.setCompanyName(request.getCompanyName());
        supplier.setPhoneNumber(request.getPhoneNumber());

        return mapper.toDto(supplierRepository.save(supplier));
    }

    @Override
    public void deleteById(Long id) {

        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        supplierRepository.delete(supplier);

    }
}
