import { Component, computed, inject, input, LOCALE_ID } from '@angular/core';
import translatePaymentType from '../../../movements/payments/utils/payment.utils';
import translatePurchaseType from '../../../movements/purchases/utils/purchase.utils';
import { formatIsoDate } from '../../../shared/utils/date.utils';
import { DashboardMovementKind, RecentMovement } from '../../interfaces/dashboard.interface';
import { formatCurrency } from '@angular/common';

// income: suma a la caja del negocio (venta / cobro). Si no, resta (compra / pago).
const KINDS: Record<DashboardMovementKind, { label: string; badge: string; income: boolean }> = {
  SALE: {
    label: 'Venta',
    badge: 'border-emerald-500/30 bg-emerald-500/10 text-emerald-300',
    income: true,
  },
  COLLECTION: {
    label: 'Cobro',
    badge: 'border-cyan-500/30 bg-cyan-500/10 text-cyan-300',
    income: true,
  },
  PURCHASE: {
    label: 'Compra',
    badge: 'border-blue-500/30 bg-blue-500/10 text-blue-300',
    income: false,
  },
  PAYMENT: {
    label: 'Pago',
    badge: 'border-rose-500/30 bg-rose-500/10 text-rose-300',
    income: false,
  },
};

@Component({
  selector: 'recent-movements-card',
  templateUrl: './recent-movements-card.html',
  host: { class: 'block h-full' },
})
export class RecentMovementsCard {
  private locale = inject(LOCALE_ID);

  movements = input.required<RecentMovement[]>();

  rows = computed(() =>
    this.movements().map((movement) => {
      const kind = KINDS[movement.kind];

      return {
        id: movement.id,
        code: `MOV-${String(movement.id).padStart(4, '0')}`,
        label: kind.label,
        badge: kind.badge,
        counterparty: movement.counterparty,
        detail: this.detail(movement),
        amount: `${kind.income ? '+' : '-'}${formatCurrency(Math.abs(movement.amount), this.locale, '$', 'ARS', '1.0-0')}`,
        amountClass: kind.income ? 'text-emerald-400' : 'text-rose-400',
      };
    }),
  );

  // "Transferencia · 26/09/2026", "Media cerdo · 25/09/2026"
  private detail(movement: RecentMovement): string {
    const text = !movement.detail
      ? ''
      : movement.kind === 'PAYMENT' || movement.kind === 'COLLECTION'
        ? translatePaymentType(movement.detail)
        : movement.kind === 'PURCHASE'
          ? translatePurchaseType(movement.detail)
          : movement.detail;

    return [text, formatIsoDate(movement.date)].filter(Boolean).join(' · ');
  }
}
