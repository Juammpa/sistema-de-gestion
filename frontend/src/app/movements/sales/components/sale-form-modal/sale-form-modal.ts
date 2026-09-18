import { Component, computed, inject, input, output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../utils/form-utils';
import { CutMeatService } from '../../../../cut-meat/services/cut-meat.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { SaleRequest } from '../../interfaces/sales.interface';
import { SaleService } from '../../services/sale.service';
import { Client } from '../../../../clientes/interfaces/client.interface';
import { ButcherShop } from '../../../../butcher-shop/interfaces/butcher-shop.interface';
import { TitleCasePipe } from '@angular/common';

@Component({
  selector: 'sale-form-modal',
  imports: [ReactiveFormsModule, TitleCasePipe],
  templateUrl: './sale-form-modal.html',
})
export class SaleFormModal {
  private SaleService = inject(SaleService);
  private cutMeatService = inject(CutMeatService);
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

  // Cortes de carnes activos
  activeCutMeats = computed(() =>
    (this.cutMeatOptions.value() ?? []).filter((cutMeat) => cutMeat.isActive),
  );

  saleForm: FormGroup = this.fb.group({
    cutMeatId: [null as number | null, [Validators.required]],
    kilograms: [0, [Validators.required, Validators.min(0)]],
    unit: [1, [Validators.required, Validators.min(1)]],
  });

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

    // Obtenemos el request
    const request: SaleRequest = this.saleForm.value;
    request.clientId = this.client().id;
    request.butcherShopId = this.butcherShop().id;

    // Guardamos la venta
    this.SaleService.createSale(request).subscribe({
      next: () => {
        this.saved.emit();
        this.saleForm.reset();
        this.close();
      },
      error: (err) => console.error('Error al guardar la venta', err),
    });
  }
}
