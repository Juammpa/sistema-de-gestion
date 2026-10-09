import { Component, computed, inject, input, OnInit, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../shared/utils/form-utils';
import { SaleService } from '../../services/sale.service';
import { CutMeatService } from '../../../../cut-meat/services/cut-meat.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { Sale, SaleRequest } from '../../interfaces/sales.interface';
import { DeleteModal } from '../../../../shared/components/delete-modal/delete-modal';
import { TitleCasePipe } from '@angular/common';
import { toIsoDate } from '../../../../shared/utils/date.utils';
import { AppValidators } from '../../../../shared/validators/app-validators';

@Component({
  selector: 'edit-sale-modal',
  imports: [ReactiveFormsModule, DeleteModal, TitleCasePipe],
  templateUrl: './edit-sale-modal.html',
})
export class EditSaleModal implements OnInit {
  private saleService = inject(SaleService);
  private cutMeatService = inject(CutMeatService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Inputs
  sale = input.required<Sale>();
  butcherShopId = input.required<number>(); // necesario para el SaleRequest del update

  // Outputs
  cancel = output();
  saved = output();
  deleted = output();

  // Arreglo con los tipos de cortes
  cutMeatOptions = rxResource({
    stream: () => this.cutMeatService.getAllCutMeats(),
  });

  // Tope del selector de fecha: no se puede elegir un dia futuro
  today = toIsoDate(new Date());

  // Cortes de carnes activos
  activeCutMeats = computed(() =>
    (this.cutMeatOptions.hasValue() ? this.cutMeatOptions.value() : []).filter(
      (cutMeat) => cutMeat.isActive,
    ),
  );

  saleForm: FormGroup = this.fb.group({
    date: [null as string | null, [Validators.required, AppValidators.notFutureDate]],
    cutMeatId: [null as number | null, [Validators.required]],
    kilograms: ['', [Validators.required, AppValidators.amount(9_999.999, 3)]],
    unit: [1, [Validators.required, AppValidators.integer(1, 9_999)]],
  });

  // Precarga el form con los datos actuales de la venta
  ngOnInit() {
    const current = this.sale();

    this.saleForm.patchValue({
      date: current.date,
      cutMeatId: current.cutMeatId,
      kilograms: current.kilograms,
      unit: current.unit,
    });
  }

  showDeleteConfirm = signal(false);
  deleting = signal(false);

  close() {
    this.cancel.emit();
  }

  onSave() {
    if (this.saleForm.invalid) {
      this.saleForm.markAllAsTouched();
      return;
    }

    const { date, cutMeatId, kilograms, unit } = this.saleForm.value;
    const request: SaleRequest = {
      date,
      cutMeatId,
      kilograms: this.formUtils.parseDecimal(kilograms)!,
      unit: this.formUtils.parseDecimal(unit)!,
      clientId: this.sale().clientId,
      butcherShopId: this.butcherShopId(),
    };

    this.saleService.updateSale(this.sale().id, request).subscribe({
      next: () => {
        this.saved.emit();
        this.close();
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }

  onDelete() {
    this.deleting.set(true);

    this.saleService.deleteSaleById(this.sale().id).subscribe({
      next: () => {
        this.deleted.emit();
        this.close();
      },
      error: () => this.deleting.set(false), // El aviso lo muestra el interceptor
    });
  }
}
