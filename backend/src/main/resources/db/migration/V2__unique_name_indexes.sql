-- Garantiza nombres únicos (en minúsculas) para registros activos, evitando condiciones de carrera.
CREATE UNIQUE INDEX uq_suppliers_company_name_active
    ON suppliers (lower(company_name))
    WHERE is_active;

-- Garantiza nombres únicos de sucursales (carnicerias) por cliente para registros activos.
CREATE UNIQUE INDEX uq_butcher_shops_client_name_active
    ON butcher_shops (client_id, lower(name))
    WHERE is_active;

-- Garantiza nombres y especies únicos en cortes de carne, incluyendo registros eliminados.
CREATE UNIQUE INDEX uq_cut_meats_name_specie
    ON cut_meats (lower(cut_meat_name), specie);