import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { CashDateFilter } from '../../../cash/components/cash-date-filter/cash-date-filter';
import { SpecieValue } from '../../../cut-meat/utils/specie.utils';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { ErrorState } from '../../../shared/components/error-state/error-state';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { daysAgo, toIsoDate } from '../../../shared/utils/date.utils';
import { StockHeader } from '../../components/stock-header/stock-header';
import { StockService } from '../../services/stock.service';
import { StockItemCard } from '../../components/stock-item/stock-item';
import { SUPPLIER_TYPE_INFO } from '../../../supplier/utils/supplier-type.utils';
import { StockWeightsModal } from '../../components/stock-weights-modal/stock-weights-modal';

type SpecieFilter = 'ALL' | SpecieValue;

@Component({
  selector: 'app-stock-page',
  imports: [
    DecimalPipe,
    CashDateFilter,
    EmptyState,
    ErrorState,
    LoadingSpinner,
    StockHeader,
    StockItemCard,
    StockWeightsModal,
  ],
  templateUrl: './stock-page.html',
  host: { class: 'flex min-h-0 flex-1 flex-col' },
})
export default class StockPage {
  private stockService = inject(StockService);

  // Por defecto: los ultimos 7 dias
  to = signal(toIsoDate(new Date()));
  from = signal(toIsoDate(daysAgo(6)));

  // Filtros de pantalla: se aplican sobre lo ya cargado, sin volver a pedir datos
  specie = signal<SpecieFilter>('ALL');
  selectedType = signal<number | null>(null);

  protected readonly specieOptions: { value: SpecieFilter; label: string }[] = [
    { value: 'ALL', label: 'Todos' },
    { value: 'BOVINE', label: 'Bovinos' },
    { value: 'PORCINE', label: 'Porcinos' },
  ];

  stockResource = rxResource({
    params: () => ({ from: this.from(), to: this.to() }),
    stream: ({ params }) => this.stockService.getStock(params.from, params.to),
  });

  // value() lanza error si el recurso fallo: se lee solo cuando hay valor
  stock = computed(() => (this.stockResource.hasValue() ? this.stockResource.value() : null));

  // Tipos para los chips: los que tuvieron compras en el periodo, limitados a la especie elegida
  availableTypes = computed(() => {
    const stock = this.stock();
    if (!stock) return [];

    const specie = this.specie();
    return stock.summary.byType.filter((totals) => specie === 'ALL' || totals.specie === specie);
  });

  // El tipo elegido solo vale si sigue disponible (al cambiar de especie o de fechas)
  activeType = computed(() => {
    const type = this.selectedType();
    return this.availableTypes().some((totals) => totals.purchaseTypeId === type) ? type : null;
  });

  visibleItems = computed(() => {
    const stock = this.stock();
    if (!stock) return [];

    const specie = this.specie();
    const type = this.activeType();
    return stock.items.filter(
      (item) =>
        (specie === 'ALL' || item.specie === specie) && (!type || item.purchaseTypeId === type),
    );
  });

  visibleKilograms = computed(() =>
    this.visibleItems().reduce((sum, item) => sum + item.kilograms, 0),
  );

  // Compra cuyas piezas se estan ajustando (null = modal cerrado)
  protected editingPurchaseId = signal<number | null>(null);

  // Todas las piezas de esa compra (sin filtros de pantalla: el repartoes de la compra completa)
  protected editingPieces = computed(() => {
    const purchaseId = this.editingPurchaseId();
    return this.stock()?.items.filter((item) => item.purchaseId === purchaseId) ?? [];
  });

  protected onWeightsSaved() {
    this.editingPurchaseId.set(null);
    this.stockResource.reload();
  }

  // Color de las insignas segun la especie elegida (el acento de la app si son todas)
  protected countBadge = computed(() => {
    const specie = this.specie();

    return specie === 'ALL'
      ? 'border-accent-500/30 bg-accent-500/10 text-accent-300'
      : SUPPLIER_TYPE_INFO[specie].badge;
  });

  // Tocar el tipo elegido lo deselecciona
  protected selectType(type: number) {
    this.selectedType.update((current) => (current === type ? null : type));
  }
}
