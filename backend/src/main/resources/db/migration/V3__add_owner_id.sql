-- Cada registro pertenece a un usuario de Keycloak, identificado por el claim "sub" (un UUID).
-- Los datos existentes se asignan al administrador inicial (placeholder initial_owner_id).

-- 1) Columna nullable para poder completar los datos que ya existen
ALTER TABLE client                 ADD COLUMN owner_id uuid;
ALTER TABLE suppliers              ADD COLUMN owner_id uuid;
ALTER TABLE cut_meats              ADD COLUMN owner_id uuid;
ALTER TABLE butcher_shops          ADD COLUMN owner_id uuid;
ALTER TABLE current_accounts       ADD COLUMN owner_id uuid;
ALTER TABLE movements              ADD COLUMN owner_id uuid;
ALTER TABLE purchase_piece_weights ADD COLUMN owner_id uuid;

-- 2) Todoo lo existente pasa a ser del administrador inicial
UPDATE client                 SET owner_id = '${initial_owner_id}'::uuid;
UPDATE suppliers              SET owner_id = '${initial_owner_id}'::uuid;
UPDATE cut_meats              SET owner_id = '${initial_owner_id}'::uuid;
UPDATE butcher_shops          SET owner_id = '${initial_owner_id}'::uuid;
UPDATE current_accounts       SET owner_id = '${initial_owner_id}'::uuid;
UPDATE movements              SET owner_id = '${initial_owner_id}'::uuid;
UPDATE purchase_piece_weights SET owner_id = '${initial_owner_id}'::uuid;

-- 3) A partir de ahora el dueño es obligatorio
ALTER TABLE client                 ALTER COLUMN owner_id SET NOT NULL;
ALTER TABLE suppliers              ALTER COLUMN owner_id SET NOT NULL;
ALTER TABLE cut_meats              ALTER COLUMN owner_id SET NOT NULL;
ALTER TABLE butcher_shops          ALTER COLUMN owner_id SET NOT NULL;
ALTER TABLE current_accounts       ALTER COLUMN owner_id SET NOT NULL;
ALTER TABLE movements              ALTER COLUMN owner_id SET NOT NULL;
ALTER TABLE purchase_piece_weights ALTER COLUMN owner_id SET NOT NULL;

-- 4) Toda consulta filtra por dueño: sin estos índices cada una recorrería la tabla entera
CREATE INDEX idx_client_owner_id                 ON client (owner_id);
CREATE INDEX idx_suppliers_owner_id              ON suppliers (owner_id);
CREATE INDEX idx_cut_meats_owner_id              ON cut_meats (owner_id);
CREATE INDEX idx_butcher_shops_owner_id          ON butcher_shops (owner_id);
CREATE INDEX idx_current_accounts_owner_id       ON current_accounts (owner_id);
CREATE INDEX idx_movements_owner_id              ON movements (owner_id);
CREATE INDEX idx_purchase_piece_weights_owner_id ON purchase_piece_weights (owner_id);

-- 5) Los nombres únicos pasan a ser únicos POR USUARIO: dos usuarios pueden tener el mismo proveedor o corte
DROP INDEX uq_suppliers_company_name_active;
CREATE UNIQUE INDEX uq_suppliers_owner_company_name_active
    ON suppliers (owner_id, lower(company_name))
    WHERE is_active;

DROP INDEX uq_cut_meats_name_specie;
CREATE UNIQUE INDEX uq_cut_meats_owner_name_specie
    ON cut_meats (owner_id, lower(cut_meat_name), specie);

-- uq_butcher_shops_client_name_active no cambia: ya incluye client_id, y cada cliente pertenece a un solo usuario