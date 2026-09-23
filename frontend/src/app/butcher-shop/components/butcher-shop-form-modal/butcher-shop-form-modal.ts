import { Component, inject, input, output } from '@angular/core';
import { ButcherShopService } from '../../services/butcher-shop.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../utils/form-utils';
import { ButcherShopRequest } from '../../interfaces/butcher-shop.interface';

@Component({
  selector: 'butcher-shop-form-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './butcher-shop-form-modal.html',
})
export class ButcherShopFormModal {
  private butcherShopService = inject(ButcherShopService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Inputs
  clientId = input.required<number>();

  // Outputs
  cancel = output();
  saved = output();

  butcherShopForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    address: ['', [Validators.required]],
  });

  close() {
    this.cancel.emit();
  }

  onSave() {
    if (this.butcherShopForm.invalid) {
      this.butcherShopForm.markAllAsTouched();
      return;
    }

    const request: ButcherShopRequest = {
      ...this.butcherShopForm.value,
      clientId: this.clientId(),
    };

    this.butcherShopService.createButcherShop(request).subscribe({
      next: () => {
        this.saved.emit();
        this.butcherShopForm.reset();
        this.close();
      },
      error: (err) => console.error('Error al crear la sucursal', err),
    });
  }
}
