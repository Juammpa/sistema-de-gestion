import { Component, computed, inject, input, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../shared/utils/form-utils';
import { CutMeatService } from '../../../../cut-meat/services/cut-meat.service';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SaleRequest } from '../../interfaces/sales.interface';
import { SaleService } from '../../services/sale.service';
import { Client } from '../../../../clientes/interfaces/client.interface';
import { ButcherShop } from '../../../../butcher-shop/interfaces/butcher-shop.interface';
import { TitleCasePipe } from '@angular/common';
import { StockService } from '../../../../stock/services/stock.service';
import { groupByCut, proposeOldest, unitsOfCut } from '../../utils/sale-units.utils';
import { SaleUnitsPicker } from '../sale-units-picker/sale-units-picker';

@Component({
  selector: 'sale-form-modal',
  imports: [ReactiveFormsModule, TitleCasePipe, SaleUnitsPicker],
  templateUrl: './sale-form-modal.html',
})
export class SaleFormModal {
  private SaleService = inject(SaleService);
  private cutMeatService = inject(CutMeatService);
  private stockService = inject(StockService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Inputs
  client = input.required<Client>();
  butcherShop = input.required<ButcherShop>();

  // Outputs
  cancel = output();
  saved = output();

  // Arreglo con los tipos de cortes
  cutMeatOptions = rxResource({
    stream: () => this.cutMeatService.getAllCutMeats(),
  });

  // Unidades de stock sin vender, de la mas antigua a la mas nueva
  availableUnits = rxResource({
    stream: () => this.stockService.getAvailableCuts(),
  });

  saleForm: FormGroup = this.fb.group({
    cutMeatId: [null as number | null, [Validators.required]],
  });

  // Corte elegido y unidades elegidas para venderlo
  cutMeatId = signal<number | null>(null);
  selectedIds = signal<number[]>([]);

  private units = computed(() =>
    this.availableUnits.hasValue() ? this.availableUnits.value() : [],
  );

  // Solo se puede vender un corte del que haya unidades disponibles
  cutOptions = computed(() => groupByCut(this.units()));

  cutUnits = computed(() => unitsOfCut(this.units(), this.cutMeatId()));

  pricePerKilo = computed(() => {
    const cutMeats = this.cutMeatOptions.hasValue() ? this.cutMeatOptions.value() : [];

    return cutMeats.find((cutMeat) => cutMeat.id === this.cutMeatId())?.pricePerKilo ?? null;
  });

  constructor() {
    // Al elegir un corte se propone la unidad mas antigua (se puede cambiar)
    this.saleForm.controls['cutMeatId'].valueChanges
      .pipe(takeUntilDestroyed())
      .subscribe((cutMeatId: number | null) => {
        this.cutMeatId.set(cutMeatId);
        this.selectedIds.set(proposeOldest(this.units(), cutMeatId, 1));
      });
  }

  // Cierra el modal
  close() {
    this.cancel.emit();
  }

  // Guarda una venta
  onSave() {
    if (this.saleForm.invalid) {
      this.saleForm.markAllAsTouched();
      return;
    }

    const request: SaleRequest = {
      cutMeatId: this.saleForm.value.cutMeatId,
      stockCutIds: this.selectedIds(),
      clientId: this.client().id,
      butcherShopId: this.butcherShop().id,
    };

    // Guardamos la venta
    this.SaleService.createSale(request).subscribe({
      next: () => {
        this.saved.emit();
        this.saleForm.reset();
        this.close();
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }
}
