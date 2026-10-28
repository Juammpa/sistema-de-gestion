import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { StockService } from '../../services/stock.service';
import { CutMeatService } from '../../../cut-meat/services/cut-meat.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { StockItem } from '../../interfaces/stock.interface';
import { rxResource } from '@angular/core/rxjs-interop';
import { formatKilograms, toCents } from '../../utils/kilograms.utils';
import { DecimalPipe, TitleCasePipe } from '@angular/common';

// Hasta 2 decimales, con coma o punto (el backend rechaza mas decimales)
const KILOGRAMS_PATTERN = /^\d{1,5}([.,]\d{1,2})?$/;

function parseKilograms(text: string): number | null {
  const clean = text.trim();
  return KILOGRAMS_PATTERN.test(clean) ? Number(clean.replace(',', '.')) : null;
}

// Una fila del formulario: lo que el usuario eligio y escribio
interface CutRow {
  id: number | null; // null = unidad nueva
  cutMeatId: number | null;
  kilograms: string;
  sold: boolean;
}

@Component({
  selector: 'stock-cuts-modal',
  imports: [DecimalPipe, TitleCasePipe],
  templateUrl: './stock-cuts-modal.html',
})
export class StockCutsModal implements OnInit {
  private stockService = inject(StockService);
  private cutMeatService = inject(CutMeatService);
  private notifications = inject(NotificationService);

  // La pieza que se divide en cortes
  item = input.required<StockItem>();

  cancel = output();
  saved = output();

  protected rows = signal<CutRow[]>([]);
  protected saving = signal(false);

  protected cutMeats = rxResource({
    stream: () => this.cutMeatService.getAllCutMeats(),
  });

  // Solo cortes activos de la misma especie que la pieza
  protected availableCutMeats = computed(() =>
    (this.cutMeats.hasValue() ? this.cutMeats.value() : []).filter(
      (cutMeat) => cutMeat.isActive && cutMeat.specie === this.item().specie,
    ),
  );

  protected pieceCents = computed(() => toCents(this.item().kilograms));

  protected sumCents = computed(() =>
    this.rows().reduce((sum, row) => sum + toCents(parseKilograms(row.kilograms) ?? 0), 0),
  );

  // > 0: merma; < 0: los cortes se pasan del peso de la pieza
  protected remainingCents = computed(() => this.pieceCents() - this.sumCents());

  protected hasInvalid = computed(() =>
    this.rows().some((row) => {
      const kilograms = parseKilograms(row.kilograms);
      return (
        row.cutMeatId === null || this.isUnavailable(row) || kilograms === null || kilograms <= 0
      );
    }),
  );

  protected fits = computed(() => this.remainingCents() >= 0);

  protected isValid = computed(() => !this.hasInvalid() && this.fits());

  protected canSave = computed(() => this.isValid() && this.cutMeats.hasValue() && !this.saving());

  protected summaryClass = computed(() =>
    this.isValid()
      ? 'border-emerald-500/30 bg-emerald-500/10 text-emerald-300'
      : 'border-amber-500/30 bg-amber-500/10 text-amber-300',
  );

  ngOnInit() {
    this.rows.set(
      this.item().cuts.map((cut) => ({
        id: cut.id,
        cutMeatId: cut.cutMeatId,
        kilograms: formatKilograms(toCents(cut.kilograms)),
        sold: cut.sold,
      })),
    );
  }

  // El corte de la fila ya no se puede usar (eliminado o de otra especie)
  protected isUnavailable(row: CutRow) {
    return (
      !row.sold &&
      row.cutMeatId !== null &&
      this.cutMeats.hasValue() &&
      !this.availableCutMeats().some((cutMeat) => cutMeat.id === row.cutMeatId)
    );
  }

  protected close() {
    this.cancel.emit();
  }

  protected addRow() {
    this.rows.update((list) => [
      ...list,
      { id: null, cutMeatId: null, kilograms: '', sold: false },
    ]);
  }

  protected removeRow(index: number) {
    this.rows.update((list) => list.filter((_, i) => i !== index));
  }

  protected setCut(index: number, event: Event) {
    const value = Number((event.target as HTMLSelectElement).value);

    this.rows.update((list) =>
      list.map((row, i) => (i === index ? { ...row, cutMeatId: value } : row)),
    );
  }

  protected setKilograms(index: number, event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.rows.update((list) =>
      list.map((row, i) => (i === index ? { ...row, kilograms: value } : row)),
    );
  }

  protected isInList(row: CutRow) {
    return this.availableCutMeats().some((cutMeat) => cutMeat.id === row.cutMeatId);
  }

  protected soldCutName(row: CutRow) {
    return this.item().cuts.find((cut) => cut.id === row.id)?.cutMeatName ?? '';
  }

  protected onSave() {
    if (!this.canSave()) return;

    this.saving.set(true);
    const cuts = this.rows().map((row) => ({
      id: row.id ?? undefined,
      cutMeatId: row.cutMeatId!,
      kilograms: toCents(parseKilograms(row.kilograms) ?? 0) / 100,
    }));

    this.stockService
      .updatePieceCuts(this.item().purchaseId, this.item().unitNumber, cuts)
      .subscribe({
        next: () => {
          this.notifications.success('Cortes actualizados');
          this.saved.emit();
        },
        error: () => this.saving.set(false), // El aviso lo muestra el interceptor
      });
  }
}
