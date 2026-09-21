// Valores exactos del enum PaymentType
export type PaymentTypeValue = 'CASH' | 'TRANSFER' | 'CHECK' | 'SUPPLIER_TRANSFER';

const PAYMENT_TYPE_LABELS: Record<PaymentTypeValue, string> = {
  CASH: 'Efectivo',
  TRANSFER: 'Transferencia',
  CHECK: 'Cheque',
  SUPPLIER_TRANSFER: 'Transferencia a proveedor',
};

// Opciones listas para el <select>
export const PAYMENT_TYPE_OPTIONS: { value: PaymentTypeValue; label: string }[] = (
  Object.keys(PAYMENT_TYPE_LABELS) as PaymentTypeValue[]
).map((value) => ({ value, label: PAYMENT_TYPE_LABELS[value] }));

// Traduce el valor recibido del backend para mostrarlo
export default function translatePaymentType(paymentType: string): string {
  return PAYMENT_TYPE_LABELS[paymentType as PaymentTypeValue] ?? paymentType;
}
