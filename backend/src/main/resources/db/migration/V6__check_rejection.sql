-- Un cheque rechazado se registra como un movimiento nuevo que apunta al cheque original
ALTER TABLE payments DROP CONSTRAINT payments_payment_type_check;
ALTER TABLE payments ADD CONSTRAINT payments_payment_type_check
    CHECK (payment_type IN ('TRANSFER', 'CASH', 'CHECK', 'IIBB', 'SUPPLIER_TRANSFER', 'CHECK_REJECTION'));

ALTER TABLE payments ADD COLUMN rejected_payment_id bigint;
ALTER TABLE payments ADD CONSTRAINT fk_payments_rejected_payment
    FOREIGN KEY (rejected_payment_id) REFERENCES payments (id);

-- Un cheque solo puede rechazarse una vez
CREATE UNIQUE INDEX uq_payments_rejected_payment
    ON payments (rejected_payment_id) WHERE rejected_payment_id IS NOT NULL;