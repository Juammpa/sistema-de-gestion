package org.sistemagestion.backend.service.supplier;

import lombok.RequiredArgsConstructor;
import org.sistemagestion.backend.common.Decimals;
import org.sistemagestion.backend.dto.supplier.SupplierRequestDto;
import org.sistemagestion.backend.dto.supplier.SupplierResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.SupplierMapper;
import org.sistemagestion.backend.model.CurrentAccount;
import org.sistemagestion.backend.model.Supplier;
import org.sistemagestion.backend.model.enums.Specie;
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

        assertCompanyNameAvailable(request.getCompanyName(), null);

        Supplier supplier = mapper.toSupplier(request);
        supplier.setCompanyName(request.getCompanyName().trim());

        CurrentAccount currentAccount = new CurrentAccount();
        currentAccount.setCurrentBalance(Decimals.MONEY_ZERO);

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

        assertCompanyNameAvailable(request.getCompanyName(), id);
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

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCompanyName(String companyName, Long excludeId) {

        String name = companyName.trim();

        return excludeId == null
                ? supplierRepository.existsByCompanyNameIgnoreCaseAndIsActiveTrue(name)
                : supplierRepository.existsByCompanyNameIgnoreCaseAndIsActiveTrueAndIdNot(name, excludeId);
    }

    // Un proveedor eliminado se trata como inexistente
    private Supplier findActive(Long id) {
        return supplierRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier","id",id));
    }

    // No se puede pasar a un tipo que contradiga las compras que el proveedor ya tiene
    private void assertTypeChangeAllowed(Supplier supplier, SupplierType newType) {

        if (supplier.getSupplierType() == newType) return;

            List<Specie> incompatible = newType.incompatibleSpecies();

        if (!incompatible.isEmpty() && purchaseRepository.existsByCurrentAccountIdAndPurchaseType_SpecieIn(supplier.getCurrentAccount().getId(), incompatible)) {
            throw new BusinessRuleException(String.format(
                    "No se puede cambiar el tipo a %s: el proveedor ya tiene compras registradas de otro tipo.",
                    newType.getLabel()
            ));
        }
    }

    // No puede haber dos proveedores activos con el mismo nombre
    private void assertCompanyNameAvailable(String companyName, Long excludeId) {

        if (existsByCompanyName(companyName, excludeId)) {
            throw new BusinessRuleException(String.format(
                    "Ya existe un proveedor con el nombre \\\"%s\\\".\"", companyName.trim()
            ));
        }
    }
}
