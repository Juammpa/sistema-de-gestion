package org.sistemagestion.backend.service.custom_payment_method;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.custom_payment_method.CustomPaymentMethodRequestDto;
import org.sistemagestion.backend.dto.custom_payment_method.CustomPaymentMethodResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.CustomPaymentMethodMapper;
import org.sistemagestion.backend.model.CustomPaymentMethod;
import org.sistemagestion.backend.repository.CustomPaymentMethodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomPaymentMethodService implements ICustomPaymentMethodService{

    // Nombres de los metodos de fabrica: no se pueden repetir para no confundir
    private static final Set<String> BUILT_IN_NAMES = Set.of(
            "efectivo", "transferencia","cheque","transferencia a proveedor","cheque rechazado"
    );

    private final CustomPaymentMethodRepository repository;
    private final CustomPaymentMethodMapper mapper;

    @Override
    @Transactional
    public CustomPaymentMethodResponseDto create(CustomPaymentMethodRequestDto request) {

        assertNameAvailable(request.getName(), null);

        CustomPaymentMethod saved = repository.save(mapper.toEntity(request));
        log.info("Método de pago creado correctamente.");

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomPaymentMethodResponseDto getById(Long id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomPaymentMethodResponseDto> getAll() {
        return repository.findAllByOrderByNameAsc().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public CustomPaymentMethodResponseDto update(Long id,  CustomPaymentMethodRequestDto request) {

        CustomPaymentMethod method = findOrThrow(id);

        assertNameAvailable(request.getName(), id);

        method.setName(request.getName().trim());
        log.info("Método de pago actualizado correctamente.");

        return mapper.toDto(repository.save(method));
    }

    @Override
    @Transactional
    public void activate(Long id) {

        CustomPaymentMethod method = findOrThrow(id);

        // Al reactivar, puede haber aparecido otro metodo activo con el mismo nombre
        assertNameAvailable(method.getName(), id);

        method.setIsActive(true);
        repository.save(method);
        log.info("Método de pago reactivado correctamente.");

    }

    @Override
    @Transactional
    public void delete(Long id) {

        CustomPaymentMethod method = findOrThrow(id);

        // Borrado logico: los pagos viejos conservan su metodo
        method.setIsActive(false);
        repository.save(method);
        log.info("Método de pago eliminado correctamente.");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name, Long excludeId) {

        String trimmed = name.trim();

        if (BUILT_IN_NAMES.contains(trimmed.toLowerCase(Locale.ROOT))) {
            return true;
        }

        return excludeId == null
                ? repository.existsByNameIgnoreCaseAndIsActiveTrue(trimmed)
                : repository.existsByNameIgnoreCaseAndIsActiveTrueAndIdNot(trimmed, excludeId);

    }



    // ======== Helpers ========

    private CustomPaymentMethod findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CustomPaymentMethod","id", id));
    }

    private void assertNameAvailable(String name, Long excludeId) {

        if (existsByName(name, excludeId)) {
            throw new BusinessRuleException(String.format(
                    "Ya existe un método de pago \"%s\".",
                    name.trim()
            ));
        }
    }
}
