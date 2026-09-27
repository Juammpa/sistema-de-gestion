import { Component, computed, inject, signal } from '@angular/core';
import { ClientService } from '../../services/cliente.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { SearchInput } from '../../../shared/components/search-input/search-input';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { SearchList } from '../../../shared/components/search-list/search-list';
import { ListItem } from '../../../shared/interfaces/list-item.interface';
import { ClientMapper } from '../../mappers/client.mapper';
import { ClientFormModal } from '../../components/client-form-modal/client-form-modal';
import { Client } from '../../interfaces/client.interface';
import { FilterChange, SearchFilter } from '../../../shared/interfaces/search-filter.interface';
import { compareText, defaultFilterValues } from '../../../shared/utils/search-filter.utils';

// Nombre tal como se muestra en la lista
const fullName = (client: Client) => `${client.lastName} ${client.name}`;

// Saldo deudor total del cliente (suma de todas sus sucursales)
const totalDebt = (client: Client) =>
  client.butcherShops.reduce((sum, shop) => sum + shop.currentBalance, 0);

@Component({
  selector: 'app-client-page',
  imports: [LoadingSpinner, SearchInput, EmptyState, SearchList, ClientFormModal],
  templateUrl: './client-page.html',
})
export default class ClientPage {
  // Inyectamos el service
  private clientService = inject(ClientService);

  // Obtiene todos los clientes
  clientResource = rxResource({
    stream: () => this.clientService.getAllClients(),
  });

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

  // Cliente ordenados segun el filtro (se copia el arreglo para no mutar el recurso)
  private sortedClients = computed(() => {
    const clients = [...(this.clientResource.value() ?? [])];

    switch (this.filterValues()['sort']) {
      case 'name-desc':
        return clients.sort((a, b) => compareText(fullName(b), fullName(a)));

      case 'recent':
        return clients.sort((a, b) => b.id - a.id);

      case 'debt':
        return clients.sort((a, b) => totalDebt(b) - totalDebt(a));

      default:
        return clients.sort((a, b) => compareText(fullName(a), fullName(b)));
    }
  });

  // Mapeamos arreglo de clientes
  clientList = computed<ListItem[]>(() => ClientMapper.mapClientsToListItems(this.sortedClients()));

  // Señal para mostrar o no el modal
  showModal = signal<boolean>(false);
}
