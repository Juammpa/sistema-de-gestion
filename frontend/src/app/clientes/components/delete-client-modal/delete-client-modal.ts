import { Component, input, output } from '@angular/core';

@Component({
  selector: 'delete-client-modal',
  imports: [],
  templateUrl: './delete-client-modal.html',
})
export class DeleteClientModal {
  // Inputs
  clientName = input<string>(''); // Nombre a mostrar en la confirmacion
  loading = input<boolean>(false); // deshabilita el boton mientras corre el DELETE

  // Output
  cancel = output();
  confirm = output(); // El usuario confirmo la eliminacion

  // Cierra el modal
  close() {
    this.cancel.emit();
  }
}
