import { Component, computed, inject, input } from '@angular/core';
import { MovementService } from '../../services/movement.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { of } from 'rxjs';
import { LoadingSpinner } from '../../../shared/components/loading-spinner/loading-spinner';
import { SaleItem } from '../../sales/components/sale-item/sale-item';
import { PaymentItem } from '../../payments/components/payment-item/payment-item';

@Component({
  selector: 'movements-table',
  imports: [LoadingSpinner, SaleItem, PaymentItem],
  templateUrl: './movements-table.html',
})
export class MovementsTable {
  private movementService = inject(MovementService);

  // Sucursal a mostrar
  butcherShopId = input<number | null>(null);

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
}
