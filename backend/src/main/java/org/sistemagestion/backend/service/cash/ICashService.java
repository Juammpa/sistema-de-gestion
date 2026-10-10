package org.sistemagestion.backend.service.cash;

import org.sistemagestion.backend.dto.cash.CashDayDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ICashService {

    List<CashDayDto> getCashDays(LocalDate from, LocalDate to);

    // Saldo actual de la caja (todoo lo cobrado, menos todoo lo pagado en efectivo a proveedores)
    BigDecimal getCurrentBalance();
}
