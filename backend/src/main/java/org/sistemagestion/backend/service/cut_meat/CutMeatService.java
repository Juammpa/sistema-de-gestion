package org.sistemagestion.backend.service.cut_meat;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sistemagestion.backend.dto.cut_meat.CutMeatRequestDto;
import org.sistemagestion.backend.dto.cut_meat.CutMeatResponseDto;
import org.sistemagestion.backend.exception.ResourceNotFoundException;
import org.sistemagestion.backend.mapper.CutMeatMapper;
import org.sistemagestion.backend.model.CutMeat;
import org.sistemagestion.backend.repository.CutMeatRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CutMeatService implements ICutMeatService{

    private final CutMeatRepository cutMeatRepository;
    private final CutMeatMapper mapper;

    @Override
    public CutMeatResponseDto createCutMeat(CutMeatRequestDto request) {

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

        cutMeat.setCutMeatName(request.getCutMeatName());
        cutMeat.setPricePerKilo(request.getPricePerKilo());
        log.info("Corte de carne actualizado correctamente");

        return mapper.toDto(cutMeatRepository.save(cutMeat));
    }

    @Override
    public void deleteCutMeatById(Long id) {

        CutMeat cutMeat = cutMeatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CutMeat","id",id));

        cutMeatRepository.delete(cutMeat);
        log.info("Corte de carne eliminado correctamente");
    }
}
