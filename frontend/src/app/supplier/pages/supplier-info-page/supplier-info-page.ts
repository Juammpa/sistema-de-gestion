import { Component, inject, OnDestroy, signal } from '@angular/core';
import { SupplierService } from '../../services/supplier.service';
import { ActivatedRoute, Router } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { SupplierHeader } from '../../components/supplier-header/supplier-header';
import { SidebarService } from '../../../shared/components/sidebar/services/sidebar.service';
import { MovementsFilters } from '../../../movements/components/movements-filters/movements-filters';
import { MovementsTable } from '../../../movements/components/movements-table/movements-table';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { SupplierFormModal } from '../../components/supplier-form-modal/supplier-form-modal';
import { DeleteModal } from '../../../shared/components/delete-modal/delete-modal';

@Component({
  selector: 'supplier-info-page',
  imports: [
    DeleteModal,
    SupplierFormModal,
    SupplierHeader,
    MovementsFilters,
    MovementsTable,
    LoadingSpinner,
    EmptyState,
  ],
  templateUrl: './supplier-info-page.html',
})
export default class SupplierInfoPage implements OnDestroy {
  private supplierService = inject(SupplierService);
  private sidebarService = inject(SidebarService);
  private router = inject(Router);

  // Contraemos el sidebar
  constructor() {
    this.sidebarService.collapse();
  }

  ngOnDestroy() {
    this.sidebarService.expand();
  }

  // Obtenemos el id del proveedor
  supplierId = inject(ActivatedRoute).snapshot.params['id'];

  supplierResource = rxResource({
    stream: () => this.supplierService.getSupplierById(this.supplierId),
  });

  // Señales para mostrar o no modal
  showModal = signal<boolean>(false);
  showRemoveModal = signal<boolean>(false);

  deleting = signal<boolean>(false); // Estado de la request de borrado

  deleteSupplier(supplierId: number) {
    this.deleting.set(true);

    this.supplierService.deleteSupplier(supplierId).subscribe({
      next: () => {
        // Volvemos al listado
        this.router.navigate(['/home/proveedores']);
      },
      error: (err) => {
        console.error('Error al eliminar el proveedor', err);
        this.deleting.set(false);
      },
    });
  }
}
