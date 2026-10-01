import { Component, computed, inject, input, OnInit, output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { SupplierService } from '../../services/supplier.service';
import { FormUtils } from '../../../shared/utils/form-utils';
import { Supplier, SupplierRequest, SupplierType } from '../../interfaces/supplier.interface';
import { SUPPLIER_TYPE_OPTIONS } from '../../utils/supplier-type.utils';

@Component({
  selector: 'supplier-form-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './supplier-form-modal.html',
})
export class SupplierFormModal implements OnInit {
  private supplierService = inject(SupplierService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;
  supplierTypeOptions = SUPPLIER_TYPE_OPTIONS;

  // Inputs
  description = input.required<string>();
  supplier = input<Supplier>(); // presente --> editar; ausente --> crear

  // Outputs
  cancel = output();
  saved = output(); // Se guardo con exito (editado o creado)

  // Definimos nuestro formulario
  supplierForm: FormGroup = this.fb.group({
    companyName: ['', [Validators.required, Validators.minLength(3)]],
    phoneNumber: ['', [Validators.required, Validators.minLength(10)]],
    supplierType: [null as SupplierType | null, [Validators.required]],
  });

  // true si estamos editando un proveedor existente
  isEdit = computed(() => !!this.supplier());

  // Si llega un proveedor, precargamos el formulario (modo edicion)
  ngOnInit() {
    const current = this.supplier();
    if (current) this.supplierForm.patchValue(current);
  }

  // Cierra el modal
  close() {
    this.cancel.emit();
  }

  // Guardar proveedor
  onSave() {
    if (this.supplierForm.invalid) {
      this.supplierForm.markAllAsTouched();
      return;
    }

    // Obtenemos el request
    const request: SupplierRequest = this.supplierForm.value;

    // Segun el request: editar o crear
    const request$ = this.isEdit()
      ? this.supplierService.updateSupplier(this.supplier()!.id, request)
      : this.supplierService.createSupplier(request);

    request$.subscribe({
      next: () => {
        this.saved.emit();
        this.supplierForm.reset();
        this.close();
      },
      error: () => {}, // El aviso lo muestra el interceptor
    });
  }
}
