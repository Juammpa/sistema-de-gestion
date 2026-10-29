package org.sistemagestion.backend.service.export;

import org.sistemagestion.backend.model.Payment;
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
            case SUPPLIER_TRANSFER -> "Transferencia a proveedor";
            case CHECK_REJECTION -> "Cheque rechazado";
            case CUSTOM -> "Personalizado";
        };
    }

    // Igual, pero un pago CUSTOM muestra el nombre del metodo que eligio el usuario
    static String paymentType(Payment payment) {
        if (payment.getPaymentType() == PaymentType.CUSTOM && payment.getCustomPaymentMethod() != null) {
            return payment.getCustomPaymentMethod().getName();
        }

        return paymentType(payment.getPaymentType());
    }
}
