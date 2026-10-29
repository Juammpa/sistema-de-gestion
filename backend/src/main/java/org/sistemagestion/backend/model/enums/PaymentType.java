package org.sistemagestion.backend.model.enums;

public enum PaymentType {
    TRANSFER,           // TRANSFERENCIA
    CASH,               // EFECTIVO
    CHECK,              // CHEQUE
    SUPPLIER_TRANSFER,  // EL CLIENTE TRANSFIRIO DIRECTAMENTE A UN PROVEEDOR
    CHECK_REJECTION,     // CHEQUE RECHAZADO (REVIERTE UN CHEQUE YA CARGADO)
    CUSTOM               // METODO DE PAGO CREADO POR EL USUARIO
}
