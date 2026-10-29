package org.sistemagestion.backend.service.custom_payment_method;

import org.sistemagestion.backend.dto.custom_payment_method.CustomPaymentMethodRequestDto;
import org.sistemagestion.backend.dto.custom_payment_method.CustomPaymentMethodResponseDto;

import java.util.List;

public interface ICustomPaymentMethodService {

    CustomPaymentMethodResponseDto create(CustomPaymentMethodRequestDto request);
    CustomPaymentMethodResponseDto getById(Long id);
    List<CustomPaymentMethodResponseDto> getAll();
    CustomPaymentMethodResponseDto update(Long id, CustomPaymentMethodRequestDto request);
    void activate(Long id);
    void delete(Long id);

    // true si el nombre ya lo usa un metodo activo o coincide con uno de los de fabrica
    boolean existsByName(String name, Long excludeId);

}
