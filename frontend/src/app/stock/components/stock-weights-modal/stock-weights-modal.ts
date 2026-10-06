import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { StockService } from '../../services/stock.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { StockItem } from '../../interfaces/stock.interface';
import translatePurchaseType from '../../../movements/purchases/utils/purchase.utils';
import { DecimalPipe } from '@angular/common';

// Se trabaja en centavos (enteros) para comparar sumas sin errores de punto flotante
const toCents = (kilograms: number) => Math.round(kilograms * 100);

// Como decimal, como lo escribe el usuario
const formatKilograms = (cents: number) => (cents / 100).toFixed(2).replace('.', ',');

// Acepta coma o punto decimal: null si no es un numero
function parseKilograms(text: string): number | null {
  const clean = text.trim().replace(',', '.');
  const value = Number(clean);
  return clean !== '' && Number.isFinite(value) ? value : null;
}

@Component({
  selector: 'stock-weights-modal',
  imports: [DecimalPipe],
  templateUrl: './stock-weights-modal.html',
})
export class StockWeightsModal implements OnInit {
  private stockService = inject(StockService);
  private notifications = inject(NotificationService);

  // Piezas de una misma compra, en orden
  items = input.required<StockItem[]>();

  cancel = output();
  saved = output();

  // Lo que el usuario escribe en cada pieza
  protected weights = signal<string[]>([]);
  protected saving = signal(false);

  protected typeLabel = computed(() => translatePurchaseType(this.items()[0].purchaseType));

  protected supplierName = computed(() => this.items()[0].supplierName);

  // Kilos de la compra: las piezas siempre suman lo mismo que la compra
  protected totalCents = computed(() =>
    this.items().reduce((sum, item) => sum + toCents(item.kilograms), 0),
  );

  private parsed = computed(() => this.weights().map(parseKilograms));

  protected sumCents = computed(() =>
    this.parsed().reduce<number>((sum, kilograms) => sum + toCents(kilograms ?? 0), 0),
  );

  // > 0: faltan kilos; < 0: sobran
  protected diffCents = computed(() => this.totalCents() - this.sumCents());

  protected hasInvalid = computed(() =>
    this.parsed().some((kilograms) => kilograms === null || kilograms <= 0),
  );

  protected isBalanced = computed(() => !this.hasInvalid() && this.diffCents() === 0);

  protected canSave = computed(() => this.isBalanced() && !this.saving());

  protected summaryClass = computed(() =>
    this.isBalanced()
      ? 'border-emerald-500/30 bg-emerald-500/10 text-emerald-300'
      : 'border-amber-500/30 bg-amber-500/10 text-amber-300',
  );

  ngOnInit() {
    this.weights.set(this.items().map((item) => formatKilograms(toCents(item.kilograms))));
  }

  protected close() {
    this.cancel.emit();
  }

  protected setWeight(index: number, event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.weights.update((list) => list.map((current, i) => (i === index ? value : current)));
  }

  // Mismo reparto que hace el backend: el resto de centavos va a la ultima pieza
  protected splitEvenly() {
    const units = this.items().length;
    const each = Math.floor(this.totalCents() / units);
    const last = this.totalCents() - each * (units - 1);

    this.weights.set(
      this.items().map((_, index) => formatKilograms(index === units - 1 ? last : each)),
    );
  }

  protected onSave() {
    if (!this.canSave()) return;

    this.saving.set(true);
    const kilograms = this.parsed().map((value) => toCents(value ?? 0) / 100);

    this.stockService.updatePieceWeights(this.items()[0].purchaseId, kilograms).subscribe({
      next: () => {
        this.notifications.success('Pesos actualizados');
        this.saved.emit();
      },
      error: () => this.saving.set(false), // El aviso lo muestra el interceptor
    });
  }
}
