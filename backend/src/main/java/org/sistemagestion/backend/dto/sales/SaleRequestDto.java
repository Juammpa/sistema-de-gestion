package org.sistemagestion.backend.dto.sales;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sistemagestion.backend.validation.OnUpdate;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SaleRequestDto {

    @NotNull(message = "El ID del cliente es obligatorio.")
    @Positive(message = "El ID del cliente debe ser positivo.")
    private Long clientId;

    @NotNull(message = "El ID de la sucursal es obligatorio.")
    @Positive(message = "El ID de la sucursal debe ser positivo.")
    private Long butcherShopId;

    @NotNull(message = "El ID del corte de carne es obligatorio.")
    @Positive(message = "El ID del corte de carne debe ser positivo.")
    private Long cutMeatId;

    // Unidades de stock que se venden (completas). Los kilos y las unidades de la venta se calculan con ellas
    @NotEmpty(message = "Elegí al menos una unidad de stock para vender.")
    @Size(max = 9999, message = "No se pueden vender más de 9.999 unidades.")
    private List<@NotNull(message = "El ID de la unidad es obligatorio.") @Positive(message = "El ID de la unidad debe ser positivo.") Long> stockCutIds;

    // Al crear se ignora (la venta se registra con la fecha de hoy)
    // Al editar es obligatoria.
    @NotNull(groups = OnUpdate.class, message = "La fecha es obligatoria.")
    @PastOrPresent(message = "La fecha no puede ser futura.")
    private LocalDate date;

}
