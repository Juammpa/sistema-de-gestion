import { Location } from '@angular/common';
import { Component, inject } from '@angular/core';

@Component({
  selector: 'back-button',
  imports: [],
  templateUrl: './back-button.html',
})
export class BackButton {
  location = inject(Location);

  // Volver a la pagina anterior
  goBack() {
    this.location.back();
  }
}
