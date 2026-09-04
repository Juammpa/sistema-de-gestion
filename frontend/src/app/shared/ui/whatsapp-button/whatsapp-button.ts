import { Component, input } from '@angular/core';

@Component({
  selector: 'whatsapp-button',
  imports: [],
  templateUrl: './whatsapp-button.html',
})
export class WhatsappButton {
  phoneNumber = input.required<string>();
}
