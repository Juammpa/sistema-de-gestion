import { Component, computed, inject, input, output, signal } from '@angular/core';
import { MovementService } from '../../services/movement.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { SaleItem } from '../../sales/components/sale-item/sale-item';
import { PaymentItem } from '../../payments/components/payment-item/payment-item';
import { EditSaleModal } from '../../sales/components/edit-sale-modal/edit-sale-modal';
import { PaymentMovement, SaleMovement } from '../../interfaces/movement.interface';
import { SaleService } from '../../sales/services/sale.service';
import { PaymentService } from '../../payments/services/payment.service';
import { EditPaymentModal } from '../../payments/components/edit-payment-modal/edit-payment-modal';

@Component({
  selector: 'movements-table',
  imports: [LoadingSpinner, SaleItem, PaymentItem, EditSaleModal, EditPaymentModal],
  templateUrl: './movements-table.html',
})
export class MovementsTable {
  private movementService = inject(MovementService);
  private saleService = inject(SaleService);
  private paymentService = inject(PaymentService);

  // Sucursal a mostrar
  butcherShopId = input<number | null>(null);

  // Avisa al padre que un movimiento cambio (para refrescar el saldo)
  movementChanged = output();

  // Se vuelve a ejecutar cada vez que cambia butcherShopId
  movementResource = rxResource({
    params: () => this.butcherShopId(),
    stream: ({ params }) =>
      params !== null ? this.movementService.getMovementsByButcherShop(params) : of([]),
  });

  movements = computed(() => this.movementResource.value() ?? []);

  // Permite al padre forzar un refresh
  reload() {
    this.movementResource.reload();
  }

  // Venta elegida para ver el detalle
  showSaleDetail = signal<boolean>(false);
  selectedSale = signal<SaleMovement | null>(null);

  // Pago elegido para ver el detalle
  showPaymentDetail = signal<boolean>(false);
  selectedPayment = signal<PaymentMovement | null>(null);

  openSaleDetail(sale: SaleMovement) {
    this.selectedSale.set(sale);
    this.showSaleDetail.set(true);
  }

  openPaymentDetail(payment: PaymentMovement) {
    this.selectedPayment.set(payment);
    this.showPaymentDetail.set(true);
  }

  // Solo pide el detalle completo (con cutMeatId) cuando hay una venta seleccionada
  saleDetail = rxResource({
    params: () => this.selectedSale()?.id ?? null,
    stream: ({ params }) =>
      params !== null ? this.saleService.getSaleById(params) : of(undefined),
  });

  // Solo pide el detalle completo cuando hay un pago seleccionado
  paymentDetail = rxResource({
    params: () => this.selectedPayment()?.id ?? null,
    stream: ({ params }) =>
      params !== null ? this.paymentService.getPaymentById(params) : of(undefined),
  });

  // Al guardar o eliminar desde el modal: cerrar, refrescar tabla, avisar al padre
  onSaleChanged() {
    this.showSaleDetail.set(false);
    this.reload();
    this.movementChanged.emit();
  }

  // Al guardar o eliminar desde el modal: cerrar, refrescar tabla, avisar al padre
  onPaymentChanged() {
    this.showPaymentDetail.set(false);
    this.reload();
    this.movementChanged.emit();
  }
}
