-- Cierra la migración a purchase_types: cada compra apunta a su tipo y desaparece el enum.

-- Si alguna compra quedó sin tipo, la migración falla en vez de perder datos en silencio
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM purchases WHERE purchase_type_id IS NULL) THEN
        RAISE EXCEPTION 'Hay compras sin purchase_type_id: revisarlas antes de aplicar esta migración';
END IF;
END $$;

-- La especie de cada compra pasa a ser la de su tipo (antes se guardaba siempre BOVINE)
UPDATE purchases p
SET specie = pt.specie
    FROM purchase_types pt
WHERE pt.id = p.purchase_type_id;

ALTER TABLE purchases ALTER COLUMN purchase_type_id SET NOT NULL;
ALTER TABLE purchases DROP COLUMN purchase_type;