import { Component, computed, inject, LOCALE_ID, OnDestroy, signal } from '@angular/core';
import { ClientService } from '../../services/cliente.service';
import { ActivatedRoute, Router } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { ClientHeader } from '../../components/client-header/client-header';
import { SidebarService } from '../../../shared/components/sidebar/services/sidebar.service';
import { MovementsFilters } from '../../../movements/components/movements-filters/movements-filters';
import { MovementsTable } from '../../../movements/components/movements-table/movements-table';
import { ClientFormModal } from '../../components/client-form-modal/client-form-modal';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { DeleteModal } from '../../../shared/components/delete-modal/delete-modal';
import {
  MovementFilterOption,
  MovementType,
} from '../../../movements/interfaces/movement.interface';
import { DateRange } from '../../../shared/interfaces/date-range.interface';
import { formatCurrency } from '@angular/common';
import { ErrorState } from '../../../shared/components/error-state/error-state';

@Component({
  selector: 'client-info-page',
  imports: [
    ClientHeader,
    MovementsFilters,
    MovementsTable,
    ClientFormModal,
    LoadingSpinner,
    DeleteModal,
    ErrorState,
  ],
  templateUrl: './client-info-page.html',
})
export default class ClientInfoPage implements OnDestroy {
  // Inyectamos el service
  private clientService = inject(ClientService);
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

  // Obtenemos el clientId
  clientId = inject(ActivatedRoute).snapshot.params['id'];

  // Obtenemos el usuario
  clientResource = rxResource({
    stream: () => this.clientService.getClientById(this.clientId),
  });

  // Filtros de tipo de movimiento de un cliente
  movementFilters: MovementFilterOption[] = [
    { value: null, label: 'Todos' },
    { value: 'SALE', label: 'Solo Ventas' },
    { value: 'PAYMENT', label: 'Solo Cobranzas' },
  ];

  // Señales
  showModal = signal<boolean>(false);
  showRemoveModal = signal<boolean>(false);
  deleting = signal<boolean>(false); // Estado de la request de borrado
  selectedButcherShopId = signal<number | null>(null);
  movementType = signal<MovementType | null>(null); // Tipo de movimiento elegido (null = todos)
  dateRange = signal<DateRange>({ from: null, to: null }); // Rango de fechas elegido

  selectedCurrentAccountId = computed(() => {
    const shopId = this.selectedButcherShopId();
    const client = this.clientResource.hasValue() ? this.clientResource.value() : null;
    return client?.butcherShops.find((bs) => bs.id === shopId)?.currentAccountId ?? null;
  });

  // Aviso del modal de eliminar: un saldo pendiente sigue contando en "Por cobrar"
  deleteWarning = computed(() => {
    const note =
      'Las ventas y cobros de sus sucursales dejarán de contarse en el Dashboard. La Caja de Ahorro no cambia.';
    const client = this.clientResource.hasValue() ? this.clientResource.value() : null;
    const balance = client?.butcherShops.reduce((sum, shop) => sum + shop.currentBalance, 0) ?? 0;

    if (Math.abs(balance) < 0.005) return note;

    const amount = formatCurrency(Math.abs(balance), this.locale, '$', 'ARS', '1.0-0');

    const detail =
      balance > 0
        ? `Este cliente tiene un saldo pendiente de ${amount}, que dejará de contarse en «Por cobrar».`
        : `Este cliente tiene un saldo a favor de ${amount}, que dejará de contarse en «Por cobrar».`;

    return `${detail} ${note}`;
  });

  deleteClient(clientId: number) {
    this.deleting.set(true);

    this.clientService.deleteClient(clientId).subscribe({
      next: () => {
        // Volvemos al listado
        this.router.navigate(['/home/clientes']);
      },
      error: () => this.deleting.set(false),
    });
  }
}
