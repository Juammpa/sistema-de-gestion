import { Component, computed, inject, OnDestroy, signal } from '@angular/core';
import { ClientService } from '../../services/cliente.service';
import { ActivatedRoute, Router } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { ClientHeader } from '../../components/client-header/client-header';
import { SidebarService } from '../../../shared/components/sidebar/services/sidebar.service';
import { MovementsFilters } from '../../../movements/components/movements-filters/movements-filters';
import { MovementsTable } from '../../../movements/components/movements-table/movements-table';
import { ClientFormModal } from '../../components/client-form-modal/client-form-modal';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { DeleteModal } from '../../../shared/components/delete-modal/delete-modal';
import {
  MovementFilterOption,
  MovementType,
} from '../../../movements/interfaces/movement.interface';

@Component({
  selector: 'client-info-page',
  imports: [
    ClientHeader,
    MovementsFilters,
    MovementsTable,
    ClientFormModal,
    EmptyState,
    LoadingSpinner,
    DeleteModal,
  ],
  templateUrl: './client-info-page.html',
})
export default class ClientInfoPage implements OnDestroy {
  // Inyectamos el service
  private clientService = inject(ClientService);
  private sidebarService = inject(SidebarService);
  private router = inject(Router);

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

  selectedCurrentAccountId = computed(() => {
    const shopId = this.selectedButcherShopId();
    return (
      this.clientResource.value()?.butcherShops.find((bs) => bs.id === shopId)?.currentAccountId ??
      null
    );
  });

  deleteClient(clientId: number) {
    this.deleting.set(true);

    this.clientService.deleteClient(clientId).subscribe({
      next: () => {
        // Volvemos al listado
        this.router.navigate(['/home/clientes']);
      },
      error: (err) => {
        console.error('Error al eliminar el cliente', err);
        this.deleting.set(false);
      },
    });
  }
}
