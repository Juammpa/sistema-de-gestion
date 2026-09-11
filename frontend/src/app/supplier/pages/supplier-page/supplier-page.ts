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

  // Mapeamos arreglo de proveedores
  supplierList = computed<ListItem[]>(() =>
    SupplierMapper.mapSuppliersToListItems(this.supplierResource.value() ?? []),
  );

  // Señal para mostrar o no el modal
  showModal = signal<boolean>(false);
}
