import { Component, inject, input, OnInit, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../../utils/form-utils';
import { SaleService } from '../../services/sale.service';
import { CutMeatService } from '../../../../cut-meat/services/cut-meat.service';
import { rxResource } from '@angular/core/rxjs-interop';
import { Sale, SaleRequest } from '../../interfaces/sales.interface';
import { DeleteModal } from '../../../../shared/components/delete-modal/delete-modal';

@Component({
  selector: 'edit-sale-modal',
  imports: [ReactiveFormsModule, DeleteModal],
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

  saleForm: FormGroup = this.fb.group({
    date: [null as string | null, [Validators.required]],
    cutMeatId: [null as number | null, [Validators.required]],
    kilograms: [0, [Validators.required, Validators.min(0)]],
    unit: [1, [Validators.required, Validators.min(1)]],
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

    const request: SaleRequest = {
      ...this.saleForm.value,
      clientId: this.sale().clientId,
      butcherShopId: this.butcherShopId(),
    };

    this.saleService.updateSale(this.sale().id, request).subscribe({
      next: () => {
        this.saved.emit();
        this.close();
      },
      error: (err) => console.error('Error al actualizar la venta', err),
    });
  }

  onDelete() {
    this.deleting.set(true);

    this.saleService.deleteSaleById(this.sale().id).subscribe({
      next: () => {
        this.deleted.emit();
        this.close();
      },
      error: (err) => {
        console.error('Error al eliminar la venta', err);
        this.deleting.set(false);
      },
    });
  }
}
