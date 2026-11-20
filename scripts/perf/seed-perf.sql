-- Carga sintetica para medir rendimiento.
-- Crea 1 cliente (1 sucursal) y 1 proveedor, cada uno con :n movimientos.
--
-- Uso:  psql -v n=5000 -f seed-perf.sql
--
-- Requisitos: ya tenes cargado al menos un cliente, un corte y un tipo de compra
-- (se toma el dueno del primer cliente, asi ves estos datos al loguearte).

\set ON_ERROR_STOP on

BEGIN;

-- Datos que se reutilizan de tu base (dueno, un corte y un tipo de compra)
CREATE TEMP TABLE perf_ctx ON COMMIT DROP AS
SELECT
    (SELECT owner_id FROM client ORDER BY id LIMIT 1)       AS owner_id,
    (SELECT id FROM cut_meats ORDER BY id LIMIT 1)          AS cut_meat_id,
    (SELECT id FROM purchase_types ORDER BY id LIMIT 1)     AS purchase_type_id;

-- Dos cuentas corrientes: una para la sucursal y otra para el proveedor
INSERT INTO current_accounts (owner_id, current_balance)
SELECT owner_id, 0 FROM perf_ctx
RETURNING id AS shop_account_id \gset

INSERT INTO current_accounts (owner_id, current_balance)
SELECT owner_id, 0 FROM perf_ctx
RETURNING id AS supplier_account_id \gset

INSERT INTO client (last_name, name, phone_number, is_active, owner_id)
SELECT 'Perf', 'Cliente ' || :n, '000', true, owner_id FROM perf_ctx
RETURNING id AS client_id \gset

INSERT INTO butcher_shops (name, address, client_id, current_account_id, is_active, owner_id)
SELECT 'Sucursal perf', 'N/A', :client_id, :shop_account_id, true, owner_id FROM perf_ctx;

INSERT INTO suppliers (company_name, phone_number, current_account_id, is_active, supplier_type, owner_id)
SELECT 'Proveedor perf ' || :n, '000', :supplier_account_id, true, 'BOTH', owner_id FROM perf_ctx;

-- Filas a insertar: :n por cuenta, repartidas en ~3 anos. 1 de cada 5 es un pago.
-- Los ids se reservan de la secuencia para poder usar el mismo en movements y en su subtabla.
CREATE TEMP TABLE perf_rows ON COMMIT DROP AS
SELECT nextval(pg_get_serial_sequence('movements', 'id')) AS id, s.*
FROM (
    SELECT
        a.acct,
        CASE a.acct WHEN 'SHOP' THEN :shop_account_id ELSE :supplier_account_id END::bigint AS account_id,
        i,
        (i % 5 = 0) AS is_payment,
        current_date - ((:n - i) * 1095 / :n) AS date,
        round(5 + (abs(hashtext('k' || a.acct || i)) % 3500) / 100.0, 3) AS kilograms,
        4000 + (abs(hashtext('p' || a.acct || i)) % 8000)                AS price_per_kilo,
        50000 + (abs(hashtext('a' || a.acct || i)) % 400000)             AS payment_amount,
        (ARRAY['Asado', 'Vacio', 'Lomo', 'Cuadril', 'Bife', 'Nalga'])[1 + i % 6] AS cut_name
    FROM generate_series(1, :n) AS i
    CROSS JOIN (VALUES ('SHOP'), ('SUPP')) AS a(acct)
    ORDER BY a.acct, i
) s;

INSERT INTO movements (id, owner_id, date, amount, balance, current_account_id)
SELECT r.id, c.owner_id, r.date,
       CASE WHEN r.is_payment THEN r.payment_amount
            ELSE round(r.kilograms * r.price_per_kilo, 2) END,
       0, r.account_id
FROM perf_rows r, perf_ctx c;

INSERT INTO sales (id, client_id, cut_meat_id, cut_meat_name, kilograms, price_per_kilo, unit)
SELECT r.id, :client_id, c.cut_meat_id, r.cut_name, r.kilograms, r.price_per_kilo, 1
FROM perf_rows r, perf_ctx c
WHERE r.acct = 'SHOP' AND NOT r.is_payment;

INSERT INTO purchases (id, purchase_type_id, specie, kilograms, price_per_kilo, unit)
SELECT r.id, c.purchase_type_id, 'BOVINE', r.kilograms, r.price_per_kilo, 1
FROM perf_rows r, perf_ctx c
WHERE r.acct = 'SUPP' AND NOT r.is_payment;

INSERT INTO payments (id, payment_type)
SELECT id, 'TRANSFER' FROM perf_rows WHERE is_payment;

-- Saldo corrido coherente: los pagos restan, el resto suma (igual que BalanceRecalculationService)
UPDATE movements m
SET balance = x.bal
FROM (
    SELECT mm.id,
           SUM(CASE WHEN p.id IS NOT NULL THEN -mm.amount ELSE mm.amount END)
               OVER (PARTITION BY mm.current_account_id ORDER BY mm.date, mm.id) AS bal
    FROM movements mm
    LEFT JOIN payments p ON p.id = mm.id
    WHERE mm.current_account_id IN (:shop_account_id, :supplier_account_id)
) x
WHERE m.id = x.id;

UPDATE current_accounts ca
SET current_balance = (
    SELECT m.balance FROM movements m
    WHERE m.current_account_id = ca.id
    ORDER BY m.date DESC, m.id DESC
    LIMIT 1)
WHERE ca.id IN (:shop_account_id, :supplier_account_id);

COMMIT;

-- Estadisticas frescas para que el planificador decida como en produccion
ANALYZE movements;
ANALYZE sales;
ANALYZE purchases;
ANALYZE payments;

SELECT 'Cliente "Perf Cliente ' || :n || '" -> cuenta ' || :shop_account_id AS cargado;
SELECT 'Proveedor "Proveedor perf ' || :n || '" -> cuenta ' || :supplier_account_id AS cargado;