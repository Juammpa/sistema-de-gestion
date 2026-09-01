import { Component, computed, inject } from '@angular/core';
import { SupplierService } from '../../services/supplier.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { SearchInput } from '../../../shared/components/search-input/search-input';
import { SearchList } from '../../../shared/components/search-list/search-list';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { ListItem } from '../../../shared/interfaces/list-item.interface';
import { SupplierMapper } from '../../mappers/supplier.mapper';

@Component({
  selector: 'app-supplier-page',
  imports: [SearchInput, SearchList, EmptyState, LoadingSpinner],
  templateUrl: './supplier-page.html',
})
export default class SupplierPage {
  supplierService = inject(SupplierService);

  supplierResource = rxResource({ stream: () => this.supplierService.getAllSuppliers() });

  // Mapeamos arreglo de proveedores
  supplierList = computed<ListItem[]>(() =>
    SupplierMapper.mapSuppliersToListItems(this.supplierResource.value() ?? []),
  );
}
