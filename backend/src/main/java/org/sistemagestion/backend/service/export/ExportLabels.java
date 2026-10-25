package org.sistemagestion.backend.service.export;

import org.sistemagestion.backend.model.enums.PaymentType;

// Textos en español que se ven en Excel
final class ExportLabels {

    private ExportLabels() {}

    static String paymentType(PaymentType type) {
        if (type == null) return "";
        return switch (type) {
            case TRANSFER -> "Transferencia";
            case CASH -> "Efectivo";
            case CHECK -> "Cheque";
            case IIBB -> "IIBB";
            case SUPPLIER_TRANSFER -> "Transferencia a proveedor";
            case CHECK_REJECTION -> "Cheque rechazado";
        };
    }
}
