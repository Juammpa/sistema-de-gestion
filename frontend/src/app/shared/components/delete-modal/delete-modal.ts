import { Component, input, output } from '@angular/core';

@Component({
  selector: 'delete-modal',
  imports: [],
  templateUrl: './delete-modal.html',
})
export class DeleteModal {
  // Inputs
  entityName = input.required<string>(); // Nombre a mostrar en la confirmacion
  loading = input<boolean>(false); //  Desabilita el boton mientras se ejecuta el DELETE
  description = input.required<string>();
  entityType = input.required<string>(); // cliente o proveedor
  permanent = input<boolean>(true); // false = borrado logico (el historial se conserva)
  warning = input<string | null>(null); // aviso extra (ej. saldo pendiente)

  // Outputs
  cancel = output(); // El usuario cancelo la eliminacion
  confirm = output(); // El usuario confirmo la eliminacion

  // Cierra el modal
  close() {
    this.cancel.emit();
  }
}
