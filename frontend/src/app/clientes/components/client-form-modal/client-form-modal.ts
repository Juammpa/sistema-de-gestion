import { Component, computed, inject, input, OnInit, output } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { FormUtils } from '../../../utils/form-utils';
import { Client, ClientCreateRequest, ClientRequest } from '../../interfaces/client.interface';
import { ClientService } from '../../services/cliente.service';

@Component({
  selector: 'client-form-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './client-form-modal.html',
})
export class ClientFormModal implements OnInit {
  private clientService = inject(ClientService);
  private fb = inject(FormBuilder);
  formUtils = FormUtils;

  // Inputs
  description = input.required<string>();
  client = input<Client>(); // presente --> editar; ausente --> crear

  // Outputs
  cancel = output();
  saved = output(); // Se guardo con exito (editado o creado)

  // Definimos nuestro formulario
  clientForm: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    lastName: ['', [Validators.required, Validators.minLength(3)]],
    phoneNumber: ['', [Validators.required, Validators.minLength(10)]],
    butcherShopName: ['', this.client() ? [] : [Validators.required, Validators.minLength(3)]],
    butcherShopAddress: ['', this.client() ? [] : [Validators.required]],
  });

  // true si estamos editanto un cliente existente
  isEdit = computed(() => !!this.client());

  // Si llega un cliente, precargamos el formulario (modo edicion)
  ngOnInit() {
    const current = this.client();
    if (current) {
      this.clientForm.patchValue(current);

      this.clientForm.removeControl('butcherShopName');
      this.clientForm.removeControl('butcherShopAddress');
    }
  }

  // Cierra el modal
  close() {
    this.cancel.emit();
  }

  // Guardar cliente
  onSave() {
    if (this.clientForm.invalid) {
      this.clientForm.markAllAsTouched();
      return;
    }

    // Obtenemos el request
    const request: ClientCreateRequest = this.clientForm.value;

    // Segun el request: editar o crear
    const request$ = this.isEdit()
      ? this.clientService.updateClient(this.client()!.id, request)
      : this.clientService.createClient(request);

    request$.subscribe({
      next: () => {
        this.saved.emit();
        this.clientForm.reset();
        this.close();
      },
      error: (err) => console.log('Error al guardar el cliente', err),
    });
  }
}
