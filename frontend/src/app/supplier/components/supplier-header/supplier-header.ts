import { Component, computed, input, output } from '@angular/core';
import { WhatsappButton } from '../../../shared/ui/whatsapp-button/whatsapp-button';
import { BackButton } from '../../../shared/ui/back-button/back-button';
import { Supplier } from '../../interfaces/supplier.interface';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'supplier-header',
  imports: [BackButton, WhatsappButton, CurrencyPipe],
  templateUrl: './supplier-header.html',
})
export class SupplierHeader {
  // Inputs
  supplier = input.required<Supplier>();

  // Outputs
  edit = output();
  remove = output();

  // Saldo a mostrar
  displayedBalance = computed(() => this.supplier().currentBalance);
}
