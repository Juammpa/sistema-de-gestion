import { Component, computed, input, output, signal } from '@angular/core';

import { CurrencyPipe } from '@angular/common';
import { WhatsappButton } from '../../../shared/ui/whatsapp-button/whatsapp-button';
import { BackButton } from '../../../shared/ui/back-button/back-button';
import { Client } from '../../interfaces/client.interface';

@Component({
  selector: 'client-header',
  imports: [WhatsappButton, BackButton, CurrencyPipe],
  templateUrl: './client-header.html',
})
export class ClientHeader {
  // Inputs
  client = input.required<Client>();

  // Outputs
  edit = output();
  remove = output();

  // null = Mostrar el total de todas las carnicerias
  selectedButcherShopId = signal<number | null>(null);

  selectButcherShop(id: number) {
    // Tocar la misma otra vez la desselecciona y vuelve al total
    this.selectedButcherShopId.update((current) => (current === id ? null : id));
  }

  // Saldo a mostrar: Se recalcula solo cuando cambia el cliente o la carniceria elegida.
  displayedBalance = computed(() => {
    const shops = this.client().butcherShops;
    // Busca la carniceria elegida.
    const selected = shops.find((bs) => bs.id === this.selectedButcherShopId());

    if (selected) return selected.currentBalance ?? 0;

    return shops.reduce((acc, bs) => acc + (bs.currentBalance ?? 0), 0);
  });

  // Nombre de la carniceria seleccionada
  selectedButcherShop = computed(() =>
    this.client().butcherShops.find((bs) => bs.id === this.selectedButcherShopId()),
  );
}
