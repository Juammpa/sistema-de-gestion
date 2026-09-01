import { Component, computed, inject } from '@angular/core';
import { ClientService } from '../../services/cliente.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { SearchInput } from '../../../shared/components/search-input/search-input';
import { EmptyState } from '../../../shared/components/empty-state/empty-state';
import { SearchList } from '../../../shared/components/search-list/search-list';
import { ListItem } from '../../../shared/interfaces/list-item.interface';
import { ClientMapper } from '../../mappers/client.mapper';

@Component({
  selector: 'app-client-page',
  imports: [LoadingSpinner, SearchInput, EmptyState, SearchList],
  templateUrl: './client-page.html',
})
export default class ClientPage {
  // Inyectamos el service
  private clientService = inject(ClientService);

  // Obtiene todos los clientes
  clientResource = rxResource({
    stream: () => this.clientService.getAllClients(),
  });

  // Mapeamos arreglo de clientes
  clientList = computed<ListItem[]>(() =>
    ClientMapper.mapClientsToListItems(this.clientResource.value() ?? []),
  );
}
