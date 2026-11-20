package org.sistemagestion.backend.service.export;

import org.sistemagestion.backend.model.enums.SupplierType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record ExportPlan(
        List<ClientPlan> clients,
        List<SupplierPlan> suppliers,
        // Cuenta de una sucursal --> nombre de su cliente (para rotular los pagos espejo)
        Map<Long, String> clientNameByAccount
) {

    public record ClientPlan(String sheetName, String fullName, List<ShopPlan> shops){}

    public record ShopPlan(String name, String address ,Long accountId, BigDecimal balance){}

    public record SupplierPlan(String companyName, SupplierType supplierType, Long accountId, BigDecimal balance) {}

}
