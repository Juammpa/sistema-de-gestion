import { Component, computed, input, signal } from '@angular/core';
import { CashDay, CashPayment } from '../../interfaces/cash.interface';
import translatePaymentType from '../../../movements/payments/utils/payment.utils';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'cash-item',
  imports: [CurrencyPipe],
  templateUrl: './cash-item.html',
})
export class CashItem {
  // Input
  day = input.required<CashDay>();

  // Señal --> La tarjeta arranca cerrada
  expanded = signal<boolean>(false);

  // Al hacer click en el header se abre el detalle.
  toggle() {
    this.expanded.update((v) => !v);
  }

  // "Miercoles 9/9/2026" (se arma la fecha a mano para evitar el desfase de zona horaria)
  title = computed(() => {
    const [year, month, day] = this.day().date.split('-').map(Number);
    const text = Intl.DateTimeFormat('es-AR', {
      weekday: 'long',
      day: 'numeric',
      month: 'numeric',
      year: 'numeric',
    }).format(new Date(year, month - 1, day));

    return text.charAt(0).toUpperCase() + text.slice(1).replace(',', '');
  });

  // Etiqueta en rojo como en la planilla (el efectivo no lleva etiqueta)
  methodLabel(payment: CashPayment): string | null {
    if (payment.paymentType === 'CASH') return null;
    if (payment.paymentType === 'SUPPLIER_TRANSFER') return `Trans. ${payment.supplierName}`;

    return translatePaymentType(payment.paymentType);
  }
}
