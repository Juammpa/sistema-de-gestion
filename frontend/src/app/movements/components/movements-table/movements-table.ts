import {
  Component,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  input,
  output,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { MovementService } from '../../services/movement.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { of, Subscription } from 'rxjs';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { SaleItem } from '../../sales/components/sale-item/sale-item';
import { PaymentItem } from '../../payments/components/payment-item/payment-item';
import { PurchaseItem } from '../../purchases/components/purchase-item/purchase-item';
import { EditSaleModal } from '../../sales/components/edit-sale-modal/edit-sale-modal';
import {
  Movement,
  PaymentMovement,
  PurchaseMovement,
  SaleMovement,
} from '../../interfaces/movement.interface';
import { SaleService } from '../../sales/services/sale.service';
import { PaymentService } from '../../payments/services/payment.service';
import { EditPaymentModal } from '../../payments/components/edit-payment-modal/edit-payment-modal';
import { PurchaseService } from '../../purchases/services/purchase.service';
import { EditPurchaseModal } from '../../purchases/components/edit-purchase-modal/edit-purchase-modal';

// Cantidad de movimientos que se piden por pagina
const PAGE_SIZE = 20;

@Component({
  selector: 'movements-table',
  imports: [
    LoadingSpinner,
    SaleItem,
    PaymentItem,
    EditSaleModal,
    EditPaymentModal,
    EditPurchaseModal,
    PurchaseItem,
  ],
  templateUrl: './movements-table.html',
})
export class MovementsTable {
  private movementService = inject(MovementService);
  private saleService = inject(SaleService);
  private paymentService = inject(PaymentService);
  private purchaseService = inject(PurchaseService);

  // Necesario para el flujo de edicion de Sale
  butcherShopId = input<number | null>(null);

  // Cuenta corriente a mostrar (sucursal o proveedor)
  currentCurrentId = input<number | null>(null);

  // Avisa al padre que un movimiento cambio (para refrescar el saldo)
  movementChanged = output();

  // ===== Scroll infinito =======
  movements = signal<Movement[]>([]); // Movimientos acumulados de todas las paginas.
  isLoading = signal<boolean>(false); // Cargando la primera pagina.
  isLoadingMore = signal<boolean>(false); // Cargando una pagina siguiente.
  hasError = signal<boolean>(false);
  hasNext = signal<boolean>(false); // Quedan mas pagina por pedir.

  private nextPage = 0;
  private request?: Subscription;

  // Bloque al final de la lista: Cuando aparece en pantalla, se pide la pagina siguiente
  private sentinel = viewChild<ElementRef<HTMLElement>>('sentinel');

  constructor() {
    // Al cambiar de cuenta corriente (sucursal o proveedor) se empieza de cero
    effect(() => {
      this.currentCurrentId();
      untracked(() => this.reload());
    });

    // Observa el bloque del final. Depende de 'movements' para volver a observar tras cada
    // pagina: Si el bloque sigue visible (pantalla alta), se pide la siguiente sin scrollear.
    effect((onCleanup) => {
      this.movements();
      const element = this.sentinel()?.nativeElement;
      if (!element) return;

      const observer = new IntersectionObserver(
        ([entry]) => {
          if (entry.isIntersecting) this.loadNextPage();
        },
        { rootMargin: '200px' },
      );
      observer.observe(element);
      onCleanup(() => observer.disconnect());
    });

    inject(DestroyRef).onDestroy(() => this.request?.unsubscribe());
  }

  // Vuelve a cargar desde la primer pagina (tambien lo usa el padre para forzar un refresh)
  reload() {
    this.request?.unsubscribe();
    this.movements.set([]);
    this.nextPage = 0;
    this.hasError.set(false);
    this.isLoading.set(false);
    this.isLoadingMore.set(false);
    this.loadNextPage();
  }

  // Pide la pagina siguiente y la agrega al final de la lista
  loadNextPage() {
    const accountId = this.currentCurrentId();
    if (accountId === null || this.isLoading() || this.isLoadingMore()) return;

    const firstPage = this.nextPage === 0;
    if (!firstPage && !this.hasNext()) return;

    (firstPage ? this.isLoading : this.isLoadingMore).set(true);
    this.hasError.set(false);

    this.request = this.movementService
      .getMovementsByCurrentAccount(accountId, this.nextPage, PAGE_SIZE)
      .subscribe({
        next: (page) => {
          this.movements.update((current) => [...current, ...page.content]);
          this.hasNext.set(page.hasNext);
          this.nextPage++;
          this.isLoading.set(false);
          this.isLoadingMore.set(false);
        },
        error: () => {
          this.hasError.set(true);
          this.isLoading.set(false);
          this.isLoadingMore.set(false);
        },
      });
  }

  // Venta elegida para ver el detalle
  showSaleDetail = signal<boolean>(false);
  selectedSale = signal<SaleMovement | null>(null);

  // Pago elegido para ver el detalle
  showPaymentDetail = signal<boolean>(false);
  selectedPayment = signal<PaymentMovement | null>(null);

  // Compra elegida para ver el detalle
  showPurchaseDetail = signal<boolean>(false);
  selectedPurchase = signal<PurchaseMovement | null>(null);

  openSaleDetail(sale: SaleMovement) {
    this.selectedSale.set(sale);
    this.showSaleDetail.set(true);
  }

  openPaymentDetail(payment: PaymentMovement) {
    this.selectedPayment.set(payment);
    this.showPaymentDetail.set(true);
  }

  openPurchaseDetail(purchase: PurchaseMovement) {
    this.selectedPurchase.set(purchase);
    this.showPurchaseDetail.set(true);
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

  // Solo pide el detalle completo cuando hay una compra seleccionado
  purchaseDetail = rxResource({
    params: () => this.selectedPurchase()?.id ?? null,
    stream: ({ params }) =>
      params !== null ? this.purchaseService.getPurchaseById(params) : of(undefined),
  });

  // Al guardar o eliminar desde el modal: cerrar, refrescar tabla, avisar al padre
  onSaleChanged() {
    this.showSaleDetail.set(false);
    this.reload();
    this.movementChanged.emit();
  }

  // Al guardar o eliminar desde el modal: cerrar, refrescar tabla, avisar al padre
  onPurchaseChanged() {
    this.showPurchaseDetail.set(false);
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
