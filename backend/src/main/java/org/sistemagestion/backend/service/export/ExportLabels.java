package org.sistemagestion.backend.service.export;

import org.sistemagestion.backend.model.enums.PaymentType;
import org.sistemagestion.backend.model.enums.PurchaseType;

// Textos en español que se ven en Excel
final class ExportLabels {

    private ExportLabels() {}

    static String purchaseType(PurchaseType type) {
        if (type == null) return "";
        return switch (type) {
            case HALF_PIG -> "Media Cerdo";
            case VEAL_UP_TO_110KG -> "Ternero hasta 110kg";
            case NT_111_120KG -> "NT 111-120kg";
            case NT_121_130KG -> "NT 121-130kg";
            case STEER_OVER_130KG -> "Novillo +130kg";
        };
    }

    static String paymentType(PaymentType type) {
        if (type == null) return "";
        return switch (type) {
            case TRANSFER -> "Transferencia";
            case CASH -> "Efectivo";
            case CHECK -> "Cheque";
            case IIBB -> "IIBB";
            case SUPPLIER_TRANSFER -> "Transferencia a proveedor";
        };
    }
}
