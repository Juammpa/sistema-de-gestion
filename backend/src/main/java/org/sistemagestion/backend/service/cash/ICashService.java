package org.sistemagestion.backend.service.cash;

import org.sistemagestion.backend.dto.cash.CashDayDto;

import java.time.LocalDate;
import java.util.List;

public interface ICashService {

    List<CashDayDto> getCashDays(LocalDate from, LocalDate to);
}
