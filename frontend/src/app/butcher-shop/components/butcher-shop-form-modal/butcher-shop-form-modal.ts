import { Component, computed, inject, input, OnInit, output } from '@angular/core';
import { ButcherShopService } from '../../services/butcher-shop.service';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../shared/utils/form-utils';
import { ButcherShop } from '../../interfaces/butcher-shop.interface';
import { AppValidators } from '../../../shared/validators/app-validators';
import { uniqueValue } from '../../../shared/validators/async-validators';
import { of } from 'rxjs';

@Component({
  selector: 'butcher-shop-form-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './butcher-shop-form-modal.html',
})
export class ButcherShopFormModal implements OnInit {
  private butcherShopService = inject(ButcherShopService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Inputs
  clientId = input<number>(); // necesario para crear la sucursal
  butcherShop = input<ButcherShop>(); // presente --> editar; ausente --> crear

  // Outputs
  cancel = output();
  saved = output();

  // Cliente dueño de la sucursal
  private ownerClientId = computed(() => this.clientId() ?? this.butcherShop()?.clientId);

  butcherShopForm: FormGroup = this.fb.group({
    name: [
      '',
      [Validators.required, AppValidators.noBlank, AppValidators.trimmedLength(2, 60)],
      [
        uniqueValue((name) => {
          const clientId = this.ownerClientId();
          return clientId === undefined
            ? of(false)
            : this.butcherShopService.nameExists(clientId, name, this.butcherShop()?.id);
        }, 'una sucursal'),
      ],
    ],
    address: [
      '',
      [Validators.required, AppValidators.noBlank, AppValidators.trimmedLength(3, 120)],
    ],
  });

  // true si estamos editando una sucursal existente
  isEdit = computed(() => !!this.butcherShop());

  // Si llega una sucursal, precargamos el formulario (modo edicion)
  ngOnInit() {
    const current = this.butcherShop();
    if (current) {
      this.butcherShopForm.patchValue({
        name: current.name,
        address: current.address,
      });
    }
  }

  close() {
    this.cancel.emit();
  }

  onSave() {
    if (this.butcherShopForm.invalid) {
      this.butcherShopForm.markAllAsTouched();
      return;
    }

    // Esperamos a que termine la validacion asincrona
    if (this.butcherShopForm.pending) return;

    const { name, address } = this.formUtils.trimValues(this.butcherShopForm.value);
    const current = this.butcherShop();

    // Segun el modo: editar o crear
    const request$ = current
      ? this.butcherShopService.updateButcherShop(current.id, { name, address })
      : this.butcherShopService.createButcherShop({ name, address, clientId: this.clientId()! });

    request$.subscribe({
      next: () => {
        this.saved.emit();
        this.butcherShopForm.reset();
        this.close();
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }
}
