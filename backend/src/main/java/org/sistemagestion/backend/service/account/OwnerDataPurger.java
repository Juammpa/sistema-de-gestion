package org.sistemagestion.backend.service.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// Borra los datos del dueño (usuario)
@Component
@RequiredArgsConstructor
@Slf4j
public class OwnerDataPurger {

    // payments, sales y purchases no tienen owner_id propio: cuelgan de movements
    private static final String OWN_MOVEMENTS = "(select id from movements where owner_id = ?)";

    private final JdbcTemplate jdbc;

    // Borra todoo lo del dueño en orden de claves foraneas (no hay DELETE CASCADE)
    // Todoo o nada: si algo falla, no se borra nada
    @Transactional
    public void purge(UUID ownerId) {
        int rows = 0;

        // Dependen de compras y ventas
        rows += deleteOwned("stock_piece_closures", ownerId);
        rows += deleteOwned("stock_cuts", ownerId);
        rows += deleteOwned("purchase_piece_weights", ownerId);

        // Un pago puede apuntar a otro (cheque rechazado y pago espejo): se sueltan antes de borrar
        jdbc.update("update payments set rejected_payment_id = null, origin_payment_id = null where id in " + OWN_MOVEMENTS, ownerId);

        // Movimientos: primero los subtipos y despues la tabla base
        rows += deleteMovementSubtype("payments", ownerId);
        rows += deleteMovementSubtype("sales", ownerId);
        rows += deleteMovementSubtype("purchases", ownerId);
        rows += deleteOwned("movements", ownerId);

        // Sucursales, proveedores y clientes van antes que las cuentas corrientes
        rows += deleteOwned("butcher_shops", ownerId);
        rows += deleteOwned("suppliers", ownerId);
        rows += deleteOwned("client", ownerId);
        rows += deleteOwned("current_accounts", ownerId);

        // Catalogos: los usaban ventas, compras y pagos
        rows += deleteOwned("cut_meats", ownerId);
        rows += deleteOwned("purchase_types", ownerId);
        rows += deleteOwned("custom_payment_methods", ownerId);

        log.info("Datos del dueño {} eliminados: {} filas", ownerId, rows);
    }

    private int deleteOwned(String table, UUID ownerId) {
        return jdbc.update("delete from " + table + " where owner_id = ?", ownerId);
    }

    private int deleteMovementSubtype(String table, UUID ownerId) {
        return jdbc.update("delete from " + table + " where id in " + OWN_MOVEMENTS, ownerId);
    }

}
