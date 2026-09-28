import { Component, computed, inject, signal } from '@angular/core';
import { SupplierService } from '../../services/supplier.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { SearchInput } from '../../../shared/components/search-input/search-input';
import { SearchList } from '../../../shared/components/search-list/search-list';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { ListItem } from '../../../shared/interfaces/list-item.interface';
import { SupplierMapper } from '../../mappers/supplier.mapper';
import { SupplierFormModal } from '../../components/supplier-form-modal/supplier-form-modal';
import { FilterChange, SearchFilter } from '../../../shared/interfaces/search-filter.interface';
import {
  compareText,
  defaultFilterValues,
  matchesSearch,
} from '../../../shared/utils/search-filter.utils';

@Component({
  selector: 'app-supplier-page',
  imports: [SearchInput, SearchList, EmptyState, LoadingSpinner, SupplierFormModal],
  templateUrl: './supplier-page.html',
})
export default class SupplierPage {
  // Inyectamos el service
  supplierService = inject(SupplierService);

  // Obtiene todos los proveedores
  supplierResource = rxResource({ stream: () => this.supplierService.getAllSuppliers() });

  // Filtros de esta pagina
  filters: SearchFilter[] = [
    {
      key: 'sort',
      label: 'Ordenar resultados',
      options: [
        { value: 'name-asc', label: 'Ordenar: nombre A-Z' },
        { value: 'name-desc', label: 'Ordenar: nombre Z-A' },
        { value: 'recent', label: 'Ordenar: más recientes' },
        { value: 'debt', label: 'Ordenar: mayor saldo deudor' },
      ],
    },
  ];

  // Valor elegido de cada filtro
  filterValues = signal<Record<string, string>>(defaultFilterValues(this.filters));

  onFilterChange({ key, value }: FilterChange) {
    this.filterValues.update((current) => ({ ...current, [key]: value }));
  }

  // Señales
  showModal = signal<boolean>(false);
  searchText = signal<string>(''); // Texto escrito en el buscador

  // Proveedores que se muestran: primero se busca (devuelve un arreglo nuevo) y despues se ordena
  private visibleSuppliers = computed(() => {
    const suppliers = (this.supplierResource.value() ?? []).filter((supplier) =>
      matchesSearch(this.searchText(), supplier.companyName, supplier.phoneNumber),
    );

    switch (this.filterValues()['sort']) {
      case 'name-desc':
        return suppliers.sort((a, b) => compareText(b.companyName, a.companyName));

      case 'recent':
        return suppliers.sort((a, b) => b.id - a.id);

      case 'debt':
        return suppliers.sort((a, b) => b.currentBalance - a.currentBalance);

      default:
        return suppliers.sort((a, b) => compareText(a.companyName, b.companyName));
    }
  });

  // Mapeamos arreglo de proveedores
  supplierList = computed<ListItem[]>(() =>
    SupplierMapper.mapSuppliersToListItems(this.visibleSuppliers()),
  );
}
