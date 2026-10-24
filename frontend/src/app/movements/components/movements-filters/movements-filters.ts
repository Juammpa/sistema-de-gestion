import { Component, computed, ElementRef, inject, input, model, signal } from '@angular/core';
import { MovementFilterOption, MovementType } from '../../interfaces/movement.interface';
import { DateRange } from '../../../shared/interfaces/date-range.interface';
import { DateField } from '../../../shared/components/date-field/date-field';
import { formatIsoDate, toIsoDate } from '../../../shared/utils/date.utils';

@Component({
  selector: 'movements-filters',
  imports: [DateField],
  templateUrl: './movements-filters.html',
  host: {
    // Cierra el panel de fechas al hacer click afuera o al apretar Escape
    '(document:click)': 'onDocumentClick($event)',
    '(document:keydown.escape)': 'closePanel()',
  },
})
export class MovementsFilters {
  private host = inject<ElementRef<HTMLElement>>(ElementRef);

  // Botones de tipo de movimiento (los define cada pagina)
  options = input.required<MovementFilterOption[]>();

  // Tipo elegido (null = todos). Se usa con [(selectedType)]
  selectedType = model<MovementType | null>(null);

  // Rango de fechas elegido (null = sin limite). Se usa con [(dateRange)]
  dateRange = model<DateRange>({ from: null, to: null });

  // Atajos de fecha (ultimos N dias, incluyendo hoy)
  presets = [
    { label: 'Hoy', days: 1 },
    { label: 'Últimos 7 días', days: 7 },
    { label: 'Últimos 30 días', days: 30 },
  ];

  // Panel de fechas y borrador de lo que se esta escribiendo (todavia sin aplicar)
  panelOpen = signal<boolean>(false);
  draftFrom = signal<string>('');
  draftTo = signal<string>('');

  hasRange = computed(() => !!(this.dateRange().from || this.dateRange().to));

  // Texto del boton
  rangeLabel = computed(() => {
    const { from, to } = this.dateRange();

    if (from && to) return `${formatIsoDate(from)} - ${formatIsoDate(to)}`;
    if (from) return `Desde ${formatIsoDate(from)}`;
    if (to) return `Hasta ${formatIsoDate(to)}`;
    return 'Todas las fechas';
  });

  // "Desde" posterior a "Hasta" (yyyy-MM-dd se puede comparar como texto)
  invalidRange = computed(
    () => !!this.draftFrom() && !!this.draftTo() && this.draftFrom() > this.draftTo(),
  );

  // Al abrir, el borrador arranca con el rango vigente
  togglePanel() {
    if (!this.panelOpen()) {
      this.draftFrom.set(this.dateRange().from ?? '');
      this.draftTo.set(this.dateRange().to ?? '');
    }

    this.panelOpen.update((open) => !open);
  }

  closePanel() {
    this.panelOpen.set(false);
  }

  onDocumentClick(event: MouseEvent) {
    // composedPath() y no contains(target): al elegir un día del calendario, ese botón ya salió
    // del DOM cuando llega este evento, y el panel se cerraría por error
    if (this.panelOpen() && !event.composedPath().includes(this.host.nativeElement)) {
      this.closePanel();
    }
  }

  // Aplica el borrador (un lado vacio significa sin limite)
  apply() {
    if (this.invalidRange()) return;

    this.dateRange.set({ from: this.draftFrom() || null, to: this.draftTo() || null });
    this.closePanel();
  }

  // Atajo: aplica directamente los ultimos 'days' dias.
  applyLastDays(days: number) {
    const start = new Date();
    start.setDate(start.getDate() - (days - 1));

    this.dateRange.set({ from: toIsoDate(start), to: toIsoDate(new Date()) });
    this.closePanel();
  }

  // Quita el filtro de fechas
  clear() {
    this.dateRange.set({ from: null, to: null });
    this.closePanel();
  }
}
