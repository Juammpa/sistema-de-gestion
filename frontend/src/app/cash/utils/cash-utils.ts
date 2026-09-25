import translatePaymentType from '../../movements/payments/utils/payment.utils';
import { CashPayment } from '../interfaces/cash.interface';

// "Miercoles 9/9/2026" (se arma la fecha a mano para evitar el desfase de zona horaria)
export function formatCashDayTitle(date: string): string {
  const [year, month, day] = date.split('-').map(Number);
  const text = Intl.DateTimeFormat('es-AR', {
    weekday: 'long',
    day: 'numeric',
    month: 'numeric',
    year: 'numeric',
  }).format(new Date(year, month - 1, day));

  return text.charAt(0).toUpperCase() + text.slice(1).replace(',', '');
}

// Etiqueta en rojo como en la plantilla (el efectivo no lleva etiqueta)
export function paymentMethodLabel(payment: CashPayment): string | null {
  if (payment.paymentType === 'CASH') return null;
  if (payment.paymentType === 'SUPPLIER_TRANSFER') return `Trans. ${payment.supplierName}`;

  return translatePaymentType(payment.paymentType);
}
