// Valores exactos del enum PaymentType
export type PaymentTypeValue =
  | 'CASH'
  | 'TRANSFER'
  | 'CHECK'
  | 'SUPPLIER_TRANSFER'
  | 'CHECK_REJECTION';

const PAYMENT_TYPE_LABELS: Record<PaymentTypeValue, string> = {
  CASH: 'Efectivo',
  TRANSFER: 'Transferencia',
  CHECK: 'Cheque',
  SUPPLIER_TRANSFER: 'Transferencia a proveedor',
  CHECK_REJECTION: 'Cheque rechazado',
};

// Opciones listas para el <select> (el rechazo lo genera el sistema, no se elige a mano)
export const PAYMENT_TYPE_OPTIONS: { value: PaymentTypeValue; label: string }[] = (
  Object.keys(PAYMENT_TYPE_LABELS) as PaymentTypeValue[]
)
  .filter((value) => value !== 'CHECK_REJECTION')
  .map((value) => ({ value, label: PAYMENT_TYPE_LABELS[value] }));

// Traduce el valor recibido del backend para mostrarlo
export default function translatePaymentType(paymentType: string): string {
  return PAYMENT_TYPE_LABELS[paymentType as PaymentTypeValue] ?? paymentType;
}
