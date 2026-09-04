import { Component, input } from '@angular/core';
import { Supplier } from '../../../interfaces/supplier.interface';
import { BackButton } from '../../../../shared/ui/back-button/back-button';
import { WhatsappButton } from '../../../../shared/ui/whatsapp-button/whatsapp-button';

@Component({
  selector: 'supplier-header',
  imports: [BackButton, WhatsappButton],
  templateUrl: './supplier-header.html',
})
export class SupplierHeader {
  supplier = input.required<Supplier>();
}
