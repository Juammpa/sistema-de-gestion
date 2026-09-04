import { Component, input } from '@angular/core';
import { Client } from '../../../interfaces/client.interface';
import { WhatsappButton } from '../../../../shared/ui/whatsapp-button/whatsapp-button';
import { BackButton } from '../../../../shared/ui/back-button/back-button';

@Component({
  selector: 'client-header',
  imports: [WhatsappButton, BackButton],
  templateUrl: './client-header.html',
})
export class ClientHeader {
  // Inputs
  client = input.required<Client>();
}
