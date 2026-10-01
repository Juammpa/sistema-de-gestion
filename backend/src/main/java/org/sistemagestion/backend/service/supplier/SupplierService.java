package org.sistemagestion.backend.service.supplier;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.dto.supplier.SupplierRequestDto;
import org.sistemagestion.backend.dto.supplier.SupplierResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.SupplierMapper;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Supplier;
import org.sistemagestion.backend.model.enums.PurchaseType;
import org.sistemagestion.backend.model.enums.SupplierType;
import org.sistemagestion.backend.repository.PurchaseRepository;
import org.sistemagestion.backend.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService implements ISupplierService{

    private final SupplierRepository supplierRepository;
    private final PurchaseRepository purchaseRepository;
    private final SupplierMapper mapper;

    @Override
    public SupplierResponseDto createSupplier(SupplierRequestDto request) {

        Supplier supplier = mapper.toSupplier(request);

        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setCurrentBalance(0.0);

        supplier.setCurrentAccount(currentAccount);

        return mapper.toDto(supplierRepository.save(supplier));
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponseDto getById(Long id) {
        return mapper.toDto(findActive(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponseDto> getAllSuppliers() {
        return supplierRepository.findByIsActiveTrue().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SupplierResponseDto updateSupplier(Long id, SupplierRequestDto request) {

        Supplier supplier = findActive(id);

        assertTypeChangeAllowed(supplier, request.getSupplierType());

        supplier.setCompanyName(request.getCompanyName());
        supplier.setPhoneNumber(request.getPhoneNumber());
        supplier.setSupplierType(request.getSupplierType());

        return mapper.toDto(supplierRepository.save(supplier));
    }

    // Borrado logico: el proveedor deja de aparecer, pero su cuenta y sus movimientos se conservan
    @Override
    @Transactional
    public void deleteById(Long id) {

        Supplier supplier = findActive(id);

        supplier.setIsActive(false);
        supplierRepository.save(supplier);
    }

    // Un proveedor eliminado se trata como inexistente
    private Supplier findActive(Long id) {
        return supplierRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier","id",id));
    }

    // No se puede pasar a un tipo que contradiga las compras que el proveedor ya tiene
    private void assertTypeChangeAllowed(Supplier supplier, SupplierType newType) {

        if (supplier.getSupplierType() == newType) return;

        List<PurchaseType> incompatible = newType.incompatiblePurchaseTypes();

        if (!incompatible.isEmpty() && purchaseRepository.existsByCurrentAccountIdAndPurchaseTypeIn(supplier.getCurrentAccount().getId(), incompatible)) {
            throw new BusinessRuleException(String.format(
                    "No se puede cambiar el tipo a %s: el proveedor ya tiene compras registradas de otro tipo.",
                    newType.getLabel()
            ));
        }
    }
}
