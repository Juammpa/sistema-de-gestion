// Valores exactos del enum PaymentType
export type PaymentTypeValue =
  | 'CASH'
  | 'TRANSFER'
  | 'CHECK'
  | 'SUPPLIER_TRANSFER'
  | 'CHECK_REJECTION'
  | 'CUSTOM';

const PAYMENT_TYPE_LABELS: Record<PaymentTypeValue, string> = {
  CASH: 'Efectivo',
  TRANSFER: 'Transferencia',
  CHECK: 'Cheque',
  SUPPLIER_TRANSFER: 'Transferencia a proveedor',
  CHECK_REJECTION: 'Cheque rechazado',
  CUSTOM: 'Personalizado', // solo se usa si falta el nombre del método
};

// Una opción del selector de método de pago
export interface PaymentMethodOption {
  value: string;
  label: string;
}

// Opciones de fábrica (el rechazo lo genera el sistema y los métodos propios los aporta el usuario)
export const PAYMENT_TYPE_OPTIONS: PaymentMethodOption[] = (
  Object.keys(PAYMENT_TYPE_LABELS) as PaymentTypeValue[]
)
  .filter((value) => value !== 'CHECK_REJECTION' && value !== 'CUSTOM')
  .map((value) => ({ value, label: PAYMENT_TYPE_LABELS[value] }));

// En el selector, un método propio se identifica como "CUSTOM:<id>" y los de fábrica con su tipo
const CUSTOM_PREFIX = 'CUSTOM:';

export function customMethodKey(id: number): string {
  return `${CUSTOM_PREFIX}${id}`;
}

// Texto del selector que corresponde a un pago ya guardado
export function paymentMethodKey(payment: {
  paymentType: string;
  customPaymentMethodId?: number | null;
}): string {
  return payment.paymentType === 'CUSTOM' && payment.customPaymentMethodId != null
    ? customMethodKey(payment.customPaymentMethodId)
    : payment.paymentType;
}

// Del texto elegido en el selector al tipo y método que espera el backend
export function parsePaymentMethodKey(key: string): {
  paymentType: string;
  customPaymentMethodId: number | null;
} {
  return key.startsWith(CUSTOM_PREFIX)
    ? { paymentType: 'CUSTOM', customPaymentMethodId: Number(key.slice(CUSTOM_PREFIX.length)) }
    : { paymentType: key, customPaymentMethodId: null };
}

// Traduce el valor recibido del backend para mostrarlo
// (en un pago CUSTOM se muestra el nombre del método que eligió el usuario)
export default function translatePaymentType(
  paymentType: string,
  customPaymentMethodName?: string | null,
): string {
  if (paymentType === 'CUSTOM' && customPaymentMethodName) return customPaymentMethodName;

  return PAYMENT_TYPE_LABELS[paymentType as PaymentTypeValue] ?? paymentType;
}
