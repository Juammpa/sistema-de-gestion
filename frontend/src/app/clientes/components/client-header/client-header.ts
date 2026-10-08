import { Component, computed, inject, input, LOCALE_ID, output, signal } from '@angular/core';

import { CurrencyPipe, formatCurrency } from '@angular/common';
import { WhatsappButton } from '../../../shared/ui/whatsapp-button/whatsapp-button';
import { BackButton } from '../../../shared/ui/back-button/back-button';
import { Client } from '../../interfaces/client.interface';
import { AddSaleButton } from '../../../movements/sales/components/add-sale-button/add-sale-button';
import { AddPaymentButton } from '../../../movements/payments/components/add-payment-button/add-payment-button';
import { SaleFormModal } from '../../../movements/sales/components/sale-form-modal/sale-form-modal';
import { PaymentFormModal } from '../../../movements/payments/components/payment-form-modal/payment-form-modal';
import { ButcherShopFormModal } from '../../../butcher-shop/components/butcher-shop-form-modal/butcher-shop-form-modal';
import { ButcherShopService } from '../../../butcher-shop/services/butcher-shop.service';
import { DeleteModal } from '../../../shared/components/delete-modal/delete-modal';

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
    ButcherShopFormModal,
    DeleteModal,
  ],
  templateUrl: './client-header.html',
})
export class ClientHeader {
  private butcherShopService = inject(ButcherShopService);
  private locale = inject(LOCALE_ID);

  // Inputs
  client = input.required<Client>();

  // Outputs
  edit = output();
  remove = output();
  saleCreated = output();
  paymentCreated = output();
  shopSelected = output<number | null>();
  butcherShopCreated = output();
  butcherShopUpdated = output();
  butcherShopDeleted = output();

  showSaleModal = signal<boolean>(false); // Modal de Venta
  showPaymentModal = signal<boolean>(false); // Modal de Pago
  showButcherShopModal = signal<boolean>(false); // Modal de Sucursal
  showEditShopModal = signal<boolean>(false); // Modal para editar sucursal
  showDeleteShopModal = signal<boolean>(false); // Modal de eliminar sucursal

  deletingShop = signal<boolean>(false); // Estado de la request de borrado

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

  // Un cliente siempre conserva al menos una sucursal
  isLastShop = computed(() => this.client().butcherShops.length <= 1);

  // Aviso del modal: el saldo pendiente de la sucursal sigue contando en "Por cobrar"
  deleteShopWarning = computed(() => {
    const note =
      'Sus ventas y cobros dejarán de contarse en el Dashboard. La Caja de Ahorro no cambia.';
    const balance = this.selectedButcherShop()?.currentBalance ?? 0;

    if (Math.abs(balance) < 0.005) return note;

    const amount = formatCurrency(Math.abs(balance), this.locale, '$', 'ARS', '1.0-0');

    const detail =
      balance > 0
        ? `Esta sucursal tiene un saldo pendiente de ${amount}, que dejará de contarse en «Por cobrar».`
        : `Esta sucursal tiene un saldo a favor de ${amount}, que dejará de contarse en «Por cobrar».`;

    return `${detail} ${note}`;
  });

  deleteSelectedShop() {
    const shop = this.selectedButcherShop();
    if (!shop) return;

    this.deletingShop.set(true);

    this.butcherShopService.deleteButcherShop(shop.id).subscribe({
      next: () => {
        this.deletingShop.set(false);
        this.showDeleteShopModal.set(false);

        // La sucursal ya no existe: volvemos a "ninguna seleccionada."
        this.selectedButcherShopId.set(null);
        this.shopSelected.emit(null);
        this.butcherShopDeleted.emit();
      },
      error: () => this.deletingShop.set(false), // El aviso lo muestra el interceptor
    });
  }

  // Valida la sucursal elegida; si no hay, muestra el aviso y corta el flujo
  private ensureShopSelected(): boolean {
    if (this.selectedButcherShopId() === null) {
      this.showShopWarning.set(true);
      return false;
    }
    return true;
  }
}
