package org.sistemagestion.backend.service.cut_meat;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.cut_meat.CutMeatRequestDto;
import org.sistemagestion.backend.dto.cut_meat.CutMeatResponseDto;
import org.sistemagestion.backend.exception.BusinessRuleException;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.CutMeatMapper;
import org.sistemagestion.backend.model.CutMeat;
import org.sistemagestion.backend.model.enums.Specie;
import org.sistemagestion.backend.repository.CutMeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CutMeatService implements ICutMeatService{

    private final CutMeatRepository cutMeatRepository;
    private final CutMeatMapper mapper;

    @Override
    public CutMeatResponseDto createCutMeat(CutMeatRequestDto request) {

        assertNameAvailable(request.getCutMeatName(), request.getSpecie(), null);

        CutMeat cutMeat = mapper.toEntity(request);
        log.info("Corte de carne creado correctamente");

        return mapper.toDto(cutMeatRepository.save(cutMeat));
    }

    @Override
    public CutMeatResponseDto getCutMeatById(Long id) {

        CutMeat cutMeat = cutMeatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CutMeat","id", id));

        return mapper.toDto(cutMeat);
    }

    @Override
    public List<CutMeatResponseDto> getAllCutMeats() {
        return cutMeatRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public CutMeatResponseDto updateCutMeat(Long id, CutMeatRequestDto request) {

        CutMeat cutMeat = cutMeatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CutMeat", "id", id));

        assertNameAvailable(request.getCutMeatName(), request.getSpecie(), id);

        cutMeat.setCutMeatName(request.getCutMeatName());
        cutMeat.setPricePerKilo(request.getPricePerKilo());
        cutMeat.setSpecie(request.getSpecie());
        log.info("Corte de carne actualizado correctamente");

        return mapper.toDto(cutMeatRepository.save(cutMeat));
    }

    @Override
    @Transactional
    public void activeCutMeatById(Long id) {

        CutMeat cutMeat = cutMeatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CutMeat", "id", id));

        cutMeat.setIsActive(true);
        cutMeatRepository.save(cutMeat);
        log.info("Corte de carne reactivado correctamente.");
    }

    @Override
    public void deleteCutMeatById(Long id) {

        CutMeat cutMeat = cutMeatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CutMeat","id",id));

        // Borrado logico
        cutMeat.setIsActive(false);
        cutMeatRepository.save(cutMeat);
        log.info("Corte de carne eliminado correctamente");
    }

    // Avisa si el nombre ya esta en uso. (excludeId: corte que se edita)
    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndSpecie(String name, Specie specie, Long excludeId) {

        String trimmed = name.trim();

        return excludeId == null
                ? cutMeatRepository.existsByCutMeatNameIgnoreCaseAndSpecie(trimmed, specie)
                : cutMeatRepository.existsByCutMeatNameIgnoreCaseAndSpecieAndIdNot(trimmed, specie, excludeId);
    }

    // No puede haber dos cortes con el mismo nombre en la misma especie (incluye eliminados)
    private void assertNameAvailable(String name, Specie specie, Long excludeId) {

        if (existsByNameAndSpecie(name, specie, excludeId)) {
            throw new BusinessRuleException(String.format(
                    "Ya existe un corte \"%s\" de esa especie. Si fue eliminado, podés reactivarlo desde su edición.",
                    name.trim()
            ));
        }
    }
}
