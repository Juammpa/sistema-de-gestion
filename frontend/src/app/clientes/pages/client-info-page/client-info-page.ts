import { Component, computed, inject } from '@angular/core';
import { ClientService } from '../../services/cliente.service';
import { ActivatedRoute } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { ClientHeader } from './client-header/client-header';

@Component({
  selector: 'client-info-page',
  imports: [ClientHeader],
  templateUrl: './client-info-page.html',
})
export default class ClientInfoPage {
  private clientService = inject(ClientService);

  // Obtenemos el clientId
  clientId = inject(ActivatedRoute).snapshot.params['id'];

  // Obtenemos el usuario
  clientResource = rxResource({
    stream: () => this.clientService.getClientById(this.clientId),
  });
}
