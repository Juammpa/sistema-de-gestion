package org.sistemagestion.backend.service.purchase_type;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.purchase_type.PurchaseTypeRequestDto;
import org.sistemagestion.backend.dto.purchase_type.PurchaseTypeResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.PurchaseTypeMapper;
import org.sistemagestion.backend.model.PurchaseType;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.repository.PurchaseTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseTypeService implements IPurchaseTypeService {

    private final PurchaseTypeRepository repository;
    private final PurchaseTypeMapper mapper;

    @Override
    @Transactional
    public PurchaseTypeResponseDto create(PurchaseTypeRequestDto request) {

        assertNameAvailable(request.getName(), request.getSpecie(), null);

        PurchaseType saved = repository.save(mapper.toEntity(request));

        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseTypeResponseDto getById(Long id) {
        return mapper.toDto(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseTypeResponseDto> getAll(Specie specie) {

        List<PurchaseType> types = specie == null
                ? repository.findAllByOrderBySpecieAscNameAsc()
                : repository.findAllBySpecieOrderByNameAsc(specie);

        return types.stream().map(mapper::toDto).toList();
    }

    @Override
    @Transactional
    public PurchaseTypeResponseDto update(Long id, PurchaseTypeRequestDto request) {

        PurchaseType type = findOrThrow(id);

        // Las compras ya registradas dependen de la especie: no se puede cambiar
        if (type.getSpecie() != request.getSpecie()) {
            throw new BusinessRuleException("La especie de un tipo de compra no se puede cambiar.");
        }

        assertNameAvailable(request.getName(), type.getSpecie(), id);

        type.setName(request.getName().trim());
        log.info("Tipo de compra actualizado correctamente.");

        return mapper.toDto(repository.save(type));
    }

    @Override
    @Transactional
    public void activate(Long id) {

        PurchaseType type = findOrThrow(id);

        // Al reactivar, puede haber aparecido otro tipo activo con el mismo nombre
        assertNameAvailable(type.getName(), type.getSpecie(), id);

        type.setIsActive(true);
        repository.save(type);
        log.info("Tipo de compra reactivado correctamente");
    }

    @Override
    @Transactional
    public void delete(Long id) {

        PurchaseType type = findOrThrow(id);

        // Borrado logico: las compras viejas conservan su tipo
        type.setIsActive(false);
        repository.save(type);
        log.info("Tipo de compra eliminado correctamente");
    }

    // Avisa si el nombre ya esta en uso por un tipo activo (excludeId: tipo que se edita)
    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndSpecie(String name, Specie specie, Long excludeId) {

        String trimmed = name.trim();

        return excludeId == null
                ? repository.existsByNameIgnoreCaseAndSpecieAndIsActiveTrue(trimmed, specie)
                : repository.existsByNameIgnoreCaseAndSpecieAndIsActiveTrueAndIdNot(trimmed, specie, excludeId);
    }

    private PurchaseType findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseType","id", id));
    }

    private void assertNameAvailable(String name, Specie specie, Long excludeId) {

        if (existsByNameAndSpecie(name, specie, excludeId)) {
            throw new BusinessRuleException(String.format(
                    "Ya existe un tipo de compra \"%s\" para esa especie.", name.trim()
            ));
        }

    }

}
