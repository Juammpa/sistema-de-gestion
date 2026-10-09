import { Component, computed, inject, input, output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../shared/utils/form-utils';
import { CutMeatService } from '../../../../cut-meat/services/cut-meat.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { SaleRequest } from '../../interfaces/sales.interface';
import { SaleService } from '../../services/sale.service';
import { Client } from '../../../../clientes/interfaces/client.interface';
import { ButcherShop } from '../../../../butcher-shop/interfaces/butcher-shop.interface';
import { TitleCasePipe } from '@angular/common';
import { AppValidators } from '../../../../shared/validators/app-validators';

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
    (this.cutMeatOptions.hasValue() ? this.cutMeatOptions.value() : []).filter(
      (cutMeat) => cutMeat.isActive,
    ),
  );

  saleForm: FormGroup = this.fb.group({
    cutMeatId: [null as number | null, [Validators.required]],
    kilograms: ['', [Validators.required, AppValidators.amount(9_999.999, 3)]],
    unit: [1, [Validators.required, AppValidators.integer(1, 9_999)]],
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

    // Armamos el request explicitamente (los números llegan como texto: se convierten acá)
    const { cutMeatId, kilograms, unit } = this.saleForm.value;
    const request: SaleRequest = {
      cutMeatId,
      kilograms: this.formUtils.parseDecimal(kilograms)!,
      unit: this.formUtils.parseDecimal(unit)!,
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
