import { Component, computed, inject, signal } from '@angular/core';
import { SearchInput } from '../../../shared/components/search-input/search-input';
import { CutMeatService } from '../../services/cut-meat.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { CutMeatListItem } from '../../components/cut-meat-list-item/cut-meat-list-item';
import { ListSkeleton } from '../../../shared/components/list-skeleton/list-skeleton';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { CutMeatFormModal } from '../../components/cut-meat-form-modal/cut-meat-form-modal';
import { EditCutMeatModal } from '../../components/edit-cut-meat-modal/edit-cut-meat-modal';
import { CutMeat } from '../../interfaces/cut-meat.interface';
import { of } from 'rxjs';
import { FilterChange, SearchFilter } from '../../../shared/interfaces/search-filter.interface';
import translateSpecie, { SPECIE_OPTIONS } from '../../utils/specie.utils';
import {
  compareText,
  defaultFilterValues,
  matchesSearch,
} from '../../../shared/utils/search-filter.utils';
import { ErrorState } from '../../../shared/components/error-state/error-state';

@Component({
  selector: 'cut-meat-page',
  imports: [
    SearchInput,
    CutMeatListItem,
    ListSkeleton,
    EmptyState,
    CutMeatFormModal,
    EditCutMeatModal,
    ErrorState,
  ],
  templateUrl: './cut-meat-page.html',
})
export default class CutMeatPage {
  private cutMeatService = inject(CutMeatService);

  cutMeatResource = rxResource({
    stream: () => this.cutMeatService.getAllCutMeats(),
  });

  // Filtros de esta pagina
  filters: SearchFilter[] = [
    {
      key: 'sort',
      label: 'Ordenar resultados',
      options: [
        { value: 'name-asc', label: 'Ordenar: nombre A-Z' },
        { value: 'name-desc', label: 'Ordenar: nombre Z-A' },
        { value: 'price-asc', label: 'Ordenar: menor precio' },
        { value: 'price-desc', label: 'Ordenar: mayor precio' },
      ],
    },
    {
      key: 'specie',
      label: 'Filtrar por especie',
      options: [{ value: 'all', label: 'Todas las especies' }, ...SPECIE_OPTIONS],
    },
    {
      key: 'status',
      label: 'Filtrar por estado',
      options: [
        { value: 'all', label: 'Activos e inactivos' },
        { value: 'active', label: 'Solo activos' },
        { value: 'inactive', label: 'Solo inactivos' },
      ],
    },
  ];

  // Valor elegido de cada filtro
  filterValues = signal<Record<string, string>>(defaultFilterValues(this.filters));

  onFilterChange({ key, value }: FilterChange) {
    this.filterValues.update((current) => ({ ...current, [key]: value }));
  }

  // Texto escrito en el buscador
  searchText = signal<string>('');

  // Cortes que se muestran: primero se filtra (devuelve un arreglo nuevo) y despues se ordena
  visibleCutMeats = computed(() => {
    const { sort, specie, status } = this.filterValues();

    const cutMeats = (this.cutMeatResource.hasValue() ? this.cutMeatResource.value() : [])
      .filter((cutMeat) =>
        matchesSearch(this.searchText(), cutMeat.cutMeatName, translateSpecie(cutMeat.specie)),
      )
      .filter((cutMeat) => specie === 'all' || cutMeat.specie === specie)
      .filter((cutMeat) => status === 'all' || cutMeat.isActive === (status === 'active'));

    switch (sort) {
      case 'name-desc':
        return cutMeats.sort((a, b) => compareText(b.cutMeatName, a.cutMeatName));

      case 'price-asc':
        return cutMeats.sort((a, b) => a.pricePerKilo - b.pricePerKilo);

      case 'price-desc':
        return cutMeats.sort((a, b) => b.pricePerKilo - a.pricePerKilo);

      default:
        return cutMeats.sort((a, b) => compareText(a.cutMeatName, b.cutMeatName));
    }
  });

  reload() {
    this.cutMeatResource.reload();
  }

  showModal = signal<boolean>(false);

  showCutMeatDetail = signal<boolean>(false);
  selectedCutMeat = signal<CutMeat | null>(null);

  openCutMeatDetail(cutMeat: CutMeat) {
    this.selectedCutMeat.set(cutMeat);
    this.showCutMeatDetail.set(true);

    if (this.cutMeatDetail.error()) this.cutMeatDetail.reload();
  }

  cutMeatDetail = rxResource({
    params: () => this.selectedCutMeat()?.id ?? null,
    stream: ({ params }) =>
      params !== null ? this.cutMeatService.getCutMeatById(params) : of(undefined),
  });

  onCutMeatChanged() {
    this.showCutMeatDetail.set(false);
    this.reload();
  }
}
