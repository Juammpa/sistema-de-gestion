import { Component, computed, input, output, signal } from '@angular/core';

import { CurrencyPipe } from '@angular/common';
import { WhatsappButton } from '../../../shared/ui/whatsapp-button/whatsapp-button';
import { BackButton } from '../../../shared/ui/back-button/back-button';
import { Client } from '../../interfaces/client.interface';
import { AddSaleButton } from '../../../movements/sales/components/add-sale-button/add-sale-button';
import { AddPaymentButton } from '../../../movements/payments/components/add-payment-button/add-payment-button';
import { SaleFormModal } from '../../../movements/sales/components/sale-form-modal/sale-form-modal';
import { PaymentFormModal } from '../../../movements/payments/components/payment-form-modal/payment-form-modal';

@Component({
  selector: 'client-header',
  imports: [
    WhatsappButton,
    BackButton,
    CurrencyPipe,
    AddSaleButton,
    AddPaymentButton,
    SaleFormModal,
    PaymentFormModal,
  ],
  templateUrl: './client-header.html',
})
export class ClientHeader {
  // Inputs
  client = input.required<Client>();

  // Outputs
  edit = output();
  remove = output();
  saleCreated = output();
  paymentCreated = output();
  shopSelected = output<number | null>();

  showSaleModal = signal<boolean>(false); // Modal de Venta
  showPaymentModal = signal<boolean>(false); // Modal de Pago

  // Aviso cuando se intenta crear una venta/pago sin elegir sucursal
  showShopWarning = signal<boolean>(false);

  // null = Mostrar el total de todas las carnicerias
  selectedButcherShopId = signal<number | null>(null);

  selectButcherShop(id: number) {
    // Tocar la misma otra vez la desselecciona y vuelve al total
    this.selectedButcherShopId.update((current) => (current === id ? null : id));
    this.showShopWarning.set(false); // al elegir sucursal, ocultamos el aviso
    this.shopSelected.emit(this.selectedButcherShopId());
  }

  // Abre el modal de venta solo si hay una sucursal seleccionada
  openSaleModal() {
    if (!this.ensureShopSelected()) return;
    this.showSaleModal.set(true);
  }

  // Abre el modal de pago solo si hay una sucursal seleccionada
  openPaymentModal() {
    if (!this.ensureShopSelected()) return;
    this.showPaymentModal.set(true);
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

  // Valida la sucursal elegida; si no hay, muestra el aviso y corta el flujo
  private ensureShopSelected(): boolean {
    if (this.selectedButcherShopId() === null) {
      this.showShopWarning.set(true);
      return false;
    }
    return true;
  }
}
