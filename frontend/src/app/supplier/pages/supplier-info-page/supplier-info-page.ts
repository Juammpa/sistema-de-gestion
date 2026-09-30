import { Component, computed, inject, LOCALE_ID, OnDestroy, signal } from '@angular/core';
import { SupplierService } from '../../services/supplier.service';
import { ActivatedRoute, Router } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { SupplierHeader } from '../../components/supplier-header/supplier-header';
import { SidebarService } from '../../../shared/components/sidebar/services/sidebar.service';
import { MovementsFilters } from '../../../movements/components/movements-filters/movements-filters';
import { MovementsTable } from '../../../movements/components/movements-table/movements-table';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { SupplierFormModal } from '../../components/supplier-form-modal/supplier-form-modal';
import { DeleteModal } from '../../../shared/components/delete-modal/delete-modal';
import {
  MovementFilterOption,
  MovementType,
} from '../../../movements/interfaces/movement.interface';
import { DateRange } from '../../../shared/interfaces/date-range.interface';
import { formatCurrency } from '@angular/common';
import { ErrorState } from '../../../shared/components/error-state/error-state';

@Component({
  selector: 'supplier-info-page',
  imports: [
    DeleteModal,
    SupplierFormModal,
    SupplierHeader,
    MovementsFilters,
    MovementsTable,
    LoadingSpinner,
    ErrorState,
  ],
  templateUrl: './supplier-info-page.html',
})
export default class SupplierInfoPage implements OnDestroy {
  private supplierService = inject(SupplierService);
  private sidebarService = inject(SidebarService);
  private router = inject(Router);
  private locale = inject(LOCALE_ID);

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

  // Filtros de tipo de movimiento de un proveedor
  movementFilters: MovementFilterOption[] = [
    { value: null, label: 'Todos' },
    { value: 'PURCHASE', label: 'Solo Compras' },
    { value: 'PAYMENT', label: 'Solo Pagos' },
  ];

  // Señales
  showModal = signal<boolean>(false);
  showRemoveModal = signal<boolean>(false);
  deleting = signal<boolean>(false); // Estado de la request de borrado
  movementType = signal<MovementType | null>(null); // Tipo de movimiento elegido (null = todos)
  dateRange = signal<DateRange>({ from: null, to: null }); // Rango de fechas elegido

  // Aviso del modal de eliminar: un saldo pendiente sigue contando en "Por pagar"
  deleteWarning = computed(() => {
    const supplier = this.supplierResource.hasValue() ? this.supplierResource.value() : null;
    const balance = supplier?.currentBalance ?? 0;

    if (Math.abs(balance) < 0.005) return null;

    const amount = formatCurrency(Math.abs(balance), this.locale, '$', 'ARS', '1.0-0');

    return balance > 0
      ? `Le debés ${amount} a este proveedor. Ese saldo seguirá contando en «Por pagar».`
      : `Este proveedor tiene un saldo a tu favor de ${amount} que seguirá restando en «Por pagar». `;
  });

  deleteSupplier(supplierId: number) {
    this.deleting.set(true);

    this.supplierService.deleteSupplier(supplierId).subscribe({
      next: () => {
        // Volvemos al listado
        this.router.navigate(['/home/proveedores']);
      },
      error: () => this.deleting.set(false),
    });
  }
}
