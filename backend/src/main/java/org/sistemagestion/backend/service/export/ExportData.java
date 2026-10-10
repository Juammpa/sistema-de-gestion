package org.sistemagestion.backend.service.export;

import org.sistemagestion.backend.model.enums.SupplierType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Todoo lo que se exporta, ya convertirdo a filas simples (sin entidades JPA)
public record ExportData(
        List<ClientSheet> clients,
        List<SupplierBook> suppliers
) {

    // Una hoja por cliente
    // Si tiene mas de una sucursal --> Un bloque por sucursal (lado a lado)
    public record ClientSheet(String sheetName, List<ShopBlock> blocks) {}

    public record ShopBlock(String title, BigDecimal balance, List<ClientRow> rows) {}

    // Fecha | Tipo | Corte | Unidad | Kilos | Precio | Entregado | Monto total | Saldo
    // highlighted: la fila se pinta (pago del cliente directo a un proveedor)
    public record ClientRow(
            LocalDate date,
            String type,
            String cutMeat,
            Integer unit,
            BigDecimal kilograms,
            BigDecimal pricePerKilo,
            BigDecimal delivered,
            BigDecimal total,
            BigDecimal balance,
            boolean highlighted
    ) {}

    // Un archivo por proveedor
    public record SupplierBook(String companyName, SupplierType supplierType, BigDecimal balance, List<SupplierRow> rows) {}

    // Fecha | Tipo | Unidad | Kilos | Precio | Debe | Haber | Saldo
    // highlighted: la fila se pinta (pago espejo, que viene de un cliente)
    public record SupplierRow(
            LocalDate date,
            String type,
            Integer unit,
            BigDecimal kilograms,
            BigDecimal pricePerKilo,
            BigDecimal debit,
            BigDecimal credit,
            BigDecimal balance,
            boolean highlighted
            ) {}
}
