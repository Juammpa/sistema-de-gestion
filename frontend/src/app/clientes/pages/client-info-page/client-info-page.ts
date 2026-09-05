import { Component, inject, OnDestroy } from '@angular/core';
import { ClientService } from '../../services/cliente.service';
import { ActivatedRoute } from '@angular/router';
import { rxResource } from '@angular/core/rxjs-interop';
import { ClientHeader } from './client-header/client-header';
import { SidebarService } from '../../../shared/components/sidebar/services/sidebar.service';

@Component({
  selector: 'client-info-page',
  imports: [ClientHeader],
  templateUrl: './client-info-page.html',
})
export default class ClientInfoPage implements OnDestroy {
  // Inyectamos el service
  private clientService = inject(ClientService);
  private sidebarService = inject(SidebarService);

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
}
