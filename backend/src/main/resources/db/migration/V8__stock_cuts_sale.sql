-- Una unidad de corte vendida apunta a su venta; sin venta, está disponible
ALTER TABLE stock_cuts ADD COLUMN sale_id bigint REFERENCES sales (id);

CREATE INDEX idx_stock_cuts_sale_id ON stock_cuts (sale_id);