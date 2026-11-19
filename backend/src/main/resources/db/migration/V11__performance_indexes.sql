-- Indices para las consultas mas frecuentes y para las claves foraneas que no tenian uno.
-- PostgreSQL no indexa las claves foraneas por si solo: sin indice, cada JOIN, cada filtro y
-- cada borrado de la tabla referenciada recorre la tabla hija completa.

-- Movimientos de una cuenta, del mas reciente al mas antiguo, dentro de un rango de fechas
-- (listado de movimientos y recalculo de saldos). Tambien cubre la clave foranea current_account_id
CREATE INDEX idx_movements_account_date ON movements (current_account_id, date DESC, id DESC);

-- Consultas por rango de fechas sin cuenta (Caja, Dashboard, Stock, movimientos recientes)
CREATE INDEX idx_movements_owner_date ON movements (owner_id, date DESC, id DESC);

-- Pagos espejo: se busca el espejo de un pago al crear, editar y borrar pagos
CREATE INDEX idx_payments_origin_payment_id ON payments (origin_payment_id) WHERE origin_payment_id IS NOT NULL;
CREATE INDEX idx_payments_supplier_id       ON payments (supplier_id)       WHERE supplier_id IS NOT NULL;

-- Ventas: claves foraneas sin indice
CREATE INDEX idx_sales_client_id   ON sales (client_id);
CREATE INDEX idx_sales_cut_meat_id ON sales (cut_meat_id);

-- Cortes disponibles (aun no vendidos): se consultan en cada venta
CREATE INDEX idx_stock_cuts_available ON stock_cuts (owner_id) WHERE sale_id IS NULL;