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

// Etiqueta del metodo de pago de cada cobro
export function paymentMethodLabel(payment: CashPayment): string {
  if (payment.paymentType === 'SUPPLIER_TRANSFER') return `Trans. ${payment.supplierName}`;

  return translatePaymentType(payment.paymentType, payment.customPaymentMethodName);
}

// Color de la tarjeta segun el metodo
export function paymentMethodBadgeClass(payment: CashPayment): string {
  switch (payment.paymentType) {
    case 'CASH':
      return 'border-emerald-500/25 bg-emerald-500/10 text-emerald-300';
    case 'SUPPLIER_TRANSFER':
      return 'border-violet-500/25 bg-violet-500/10 text-violet-300';
    case 'TRANSFER':
      return 'border-sky-500/25 bg-sky-500/10 text-sky-300';
    case 'CHECK':
      return 'border-amber-500/25 bg-amber-500/10 text-amber-300';
    default:
      return 'border-rose-500/25 bg-rose-500/10 text-rose-300';
  }
}
